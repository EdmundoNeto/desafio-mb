package br.com.edmundo.desafiomb.core.data.repository

import androidx.room.withTransaction
import br.com.edmundo.desafiomb.core.data.local.CmcDatabase
import br.com.edmundo.desafiomb.core.data.local.dao.CacheMetaDao
import br.com.edmundo.desafiomb.core.data.local.dao.ExchangeAssetDao
import br.com.edmundo.desafiomb.core.data.local.dao.ExchangeDao
import br.com.edmundo.desafiomb.core.data.local.dao.ExchangeDetailDao
import br.com.edmundo.desafiomb.core.data.local.dao.ExchangeIndexDao
import br.com.edmundo.desafiomb.core.data.local.entity.CacheMetaEntity
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeEntity
import br.com.edmundo.desafiomb.core.data.local.entity.assetsSyncKey
import br.com.edmundo.desafiomb.core.data.mapper.toDetailEntity
import br.com.edmundo.desafiomb.core.data.mapper.toDomain
import br.com.edmundo.desafiomb.core.data.mapper.toEntity
import br.com.edmundo.desafiomb.core.data.mapper.toExchangeDetail
import br.com.edmundo.desafiomb.core.data.mapper.toExchangeEntity
import br.com.edmundo.desafiomb.core.data.mapper.toIndexEntity
import br.com.edmundo.desafiomb.core.data.remote.CmcExchangeApi
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeMapItemDto
import br.com.edmundo.desafiomb.core.data.remote.safeApiCall
import br.com.edmundo.desafiomb.core.data.util.TtlPolicy
import br.com.edmundo.desafiomb.core.domain.model.Exchange
import br.com.edmundo.desafiomb.core.domain.model.ExchangeAsset
import br.com.edmundo.desafiomb.core.domain.model.ExchangeDetail
import br.com.edmundo.desafiomb.core.domain.model.PageLoad
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import br.com.edmundo.desafiomb.core.domain.util.TimeProvider
import br.com.edmundo.desafiomb.core.domain.util.fold
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

const val INFO_BLOCK_SIZE = 100

class ExchangeRepositoryImpl(
    private val api: CmcExchangeApi,
    private val db: CmcDatabase,
    private val indexDao: ExchangeIndexDao,
    private val exchangeDao: ExchangeDao,
    private val detailDao: ExchangeDetailDao,
    private val assetDao: ExchangeAssetDao,
    private val cacheMetaDao: CacheMetaDao,
    private val timeProvider: TimeProvider,
    private val ioDispatcher: CoroutineDispatcher,
) : ExchangeRepository {
    override fun observeExchanges(): Flow<List<Exchange>> =
        exchangeDao
            .observeHydrated()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun loadPage(
        page: Int,
        pageSize: Int,
    ): DomainResult<PageLoad> =
        withContext(ioDispatcher) {
            val indexResult = ensureIndex()
            if (indexResult is DomainResult.Failure) return@withContext DomainResult.failure(indexResult.error)

            val offset = page * pageSize
            val pageIds = indexDao.idsForRange(offset, pageSize)
            if (pageIds.isEmpty()) {
                return@withContext DomainResult.success(PageLoad.Fresh(hasMore = false))
            }

            val now = timeProvider.now()
            val cachedByPageId = exchangeDao.getByIds(pageIds).associateBy { it.id }
            val hasMore = hasMore(offset, pageSize)

            if (isPageFresh(pageIds, cachedByPageId, now)) {
                return@withContext DomainResult.success(PageLoad.Fresh(hasMore))
            }

            refreshInfoBlock(offset, pageIds, cachedByPageId, now, hasMore)
        }

    private fun isPageFresh(
        pageIds: List<Int>,
        cachedByPageId: Map<Int, ExchangeEntity>,
        now: Long,
    ): Boolean =
        pageIds.none { id ->
            val entity = cachedByPageId[id]
            entity == null || TtlPolicy.isStale(entity.updatedAt, TtlPolicy.LIST, now)
        }

    private suspend fun refreshInfoBlock(
        offset: Int,
        pageIds: List<Int>,
        cachedByPageId: Map<Int, ExchangeEntity>,
        now: Long,
        hasMore: Boolean,
    ): DomainResult<PageLoad> {
        val blockStart = (offset / INFO_BLOCK_SIZE) * INFO_BLOCK_SIZE
        val blockIds = indexDao.idsForRange(blockStart, INFO_BLOCK_SIZE)

        val infoResult =
            safeApiCall(
                call = { api.getExchangeInfo(ids = blockIds.joinToString(",")) },
                transform = { data -> data.values.toList() },
            )

        return when (infoResult) {
            is DomainResult.Success -> {
                exchangeDao.upsertAll(infoResult.value.map { it.toExchangeEntity(now) })
                DomainResult.success(PageLoad.Fresh(hasMore))
            }

            is DomainResult.Failure -> {
                val hasCacheForPage = pageIds.any { cachedByPageId.containsKey(it) }
                if (hasCacheForPage) {
                    DomainResult.success(PageLoad.Cached(hasMore, infoResult.error))
                } else {
                    DomainResult.failure(infoResult.error)
                }
            }
        }
    }

    private suspend fun hasMore(
        offset: Int,
        pageSize: Int,
    ): Boolean = offset + pageSize < indexDao.indexSize()

    private suspend fun ensureIndex(): DomainResult<Unit> {
        val now = timeProvider.now()
        val size = indexDao.indexSize()
        val oldestSync = indexDao.oldestIndexSync()
        val needsRefresh = size == 0 || (oldestSync != null && TtlPolicy.isStale(oldestSync, TtlPolicy.INDEX, now))
        if (!needsRefresh) return DomainResult.success(Unit)

        val mapResult = safeApiCall(call = { api.getExchangeMap() }, transform = { it })
        return mapResult.fold(
            onSuccess = { items ->
                replaceIndex(items, now)
                DomainResult.success(Unit)
            },
            onFailure = { error ->
                if (size > 0) DomainResult.success(Unit) else DomainResult.failure(error)
            },
        )
    }

    private suspend fun replaceIndex(
        items: List<ExchangeMapItemDto>,
        now: Long,
    ) {
        db.withTransaction {
            indexDao.clear()
            indexDao.insertAll(items.mapIndexed { rank, dto -> dto.toIndexEntity(rank, now) })
        }
    }

    override suspend fun refresh(): DomainResult<Unit> =
        withContext(ioDispatcher) {
            val now = timeProvider.now()
            val mapResult = safeApiCall(call = { api.getExchangeMap() }, transform = { it })
            mapResult.fold(
                onSuccess = { items ->
                    db.withTransaction {
                        indexDao.clear()
                        indexDao.insertAll(items.mapIndexed { rank, dto -> dto.toIndexEntity(rank, now) })
                        exchangeDao.deleteOrphans()
                    }
                    DomainResult.success(Unit)
                },
                onFailure = { error -> DomainResult.failure(error) },
            )
        }

    override fun observeExchangeDetail(id: Int): Flow<ExchangeDetail?> =
        combine(exchangeDao.observeById(id), detailDao.observeById(id)) { base, detail ->
            base?.let { toExchangeDetail(it, detail) }
        }.flowOn(ioDispatcher)

    override suspend fun syncExchangeDetail(id: Int): DomainResult<Unit> =
        withContext(ioDispatcher) {
            val now = timeProvider.now()
            val cached = detailDao.getById(id)
            if (cached != null && !TtlPolicy.isStale(cached.updatedAt, TtlPolicy.DETAIL, now)) {
                return@withContext DomainResult.success(Unit)
            }

            safeApiCall(
                call = { api.getExchangeInfo(ids = id.toString()) },
                transform = { data -> data.values.first() },
            ).fold(
                onSuccess = { dto ->
                    db.withTransaction {
                        exchangeDao.upsertAll(listOf(dto.toExchangeEntity(now)))
                        detailDao.upsert(dto.toDetailEntity(now))
                    }
                    DomainResult.success(Unit)
                },
                onFailure = { error -> DomainResult.failure(error) },
            )
        }

    override suspend fun getExchangeAssets(id: Int): DomainResult<List<ExchangeAsset>> =
        withContext(ioDispatcher) {
            val now = timeProvider.now()
            val syncKey = assetsSyncKey(id)
            val syncedAt = cacheMetaDao.getSyncedAt(syncKey)
            if (syncedAt != null && !TtlPolicy.isStale(syncedAt, TtlPolicy.ASSETS, now)) {
                return@withContext DomainResult.success(assetDao.getByExchangeId(id).map { it.toDomain() })
            }

            safeApiCall(
                call = { api.getExchangeAssets(id) },
                transform = { it },
            ).fold(
                onSuccess = { dtos ->
                    val entities = dtos.map { it.toEntity(id, now) }
                    db.withTransaction {
                        assetDao.deleteByExchangeId(id)
                        assetDao.insertAll(entities)
                        cacheMetaDao.upsert(CacheMetaEntity(key = syncKey, syncedAt = now))
                    }
                    DomainResult.success(entities.sortedByDescending { it.currencyPriceUsd }.map { it.toDomain() })
                },
                onFailure = { error -> DomainResult.failure(error) },
            )
        }
}
