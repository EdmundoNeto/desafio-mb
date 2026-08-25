package br.com.edmundo.desafiomb.core.data.repository

import app.cash.turbine.test
import br.com.edmundo.desafiomb.core.data.local.CmcDatabase
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeDetailEntity
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeEntity
import br.com.edmundo.desafiomb.core.data.local.entity.ExchangeIndexEntity
import br.com.edmundo.desafiomb.core.data.remote.dto.AssetCurrencyDto
import br.com.edmundo.desafiomb.core.data.remote.dto.CmcResponse
import br.com.edmundo.desafiomb.core.data.remote.dto.CmcStatus
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeAssetDto
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeInfoDto
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeMapItemDto
import br.com.edmundo.desafiomb.core.domain.error.AppError
import br.com.edmundo.desafiomb.core.domain.model.PageLoad
import br.com.edmundo.desafiomb.core.testing.fake.FakeTimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@RunWith(RobolectricTestRunner::class)
class ExchangeRepositoryCacheTest {
    private lateinit var db: CmcDatabase
    private lateinit var api: FakeCmcExchangeApi
    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var repository: ExchangeRepositoryImpl

    @Before
    fun setUp() {
        db = createInMemoryDatabase()
        api = FakeCmcExchangeApi()
        timeProvider = FakeTimeProvider(currentMillis = 1_000_000L)
        repository =
            ExchangeRepositoryImpl(
                api = api,
                db = db,
                indexDao = db.exchangeIndexDao(),
                exchangeDao = db.exchangeDao(),
                detailDao = db.exchangeDetailDao(),
                assetDao = db.exchangeAssetDao(),
                cacheMetaDao = db.cacheMetaDao(),
                timeProvider = timeProvider,
                ioDispatcher = Dispatchers.Unconfined,
            )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun seedIndexAndExchange(
        id: Int,
        syncedAt: Long,
        updatedAt: Long,
    ) {
        db.exchangeIndexDao().insertAll(listOf(ExchangeIndexEntity(id = id, rank = 0, name = "Exchange $id", syncedAt = syncedAt)))
        db.exchangeDao().upsertAll(
            listOf(
                ExchangeEntity(
                    id = id,
                    name = "Exchange $id",
                    slug = null,
                    logoUrl = null,
                    spotVolumeUsd = 1.0,
                    dateLaunchedEpochMs = null,
                    updatedAt = updatedAt,
                ),
            ),
        )
    }

    @Test
    fun `dado cache preenchido, quando observeExchanges, entao emite do cache antes de qualquer chamada de rede`() =
        runTest {
            seedIndexAndExchange(id = 1, syncedAt = timeProvider.currentMillis, updatedAt = timeProvider.currentMillis)

            repository.observeExchanges().test {
                val emitted = awaitItem()
                assertEquals(1, emitted.size)
                assertEquals(1, emitted.first().id)
            }
            assertEquals(0, api.mapCallCount)
            assertEquals(0, api.infoCallCount)
        }

    @Test
    fun `dado indice expirado, quando loadPage, entao dispara nova chamada ao map`() =
        runTest {
            val staleSyncedAt = timeProvider.currentMillis - 20.minutes.inWholeMilliseconds
            seedIndexAndExchange(id = 1, syncedAt = staleSyncedAt, updatedAt = timeProvider.currentMillis)
            api.onGetExchangeMap =
                { CmcResponse(CmcStatus(), listOf(ExchangeMapItemDto(id = 1, name = "Exchange 1", slug = "exchange-1"))) }
            api.onGetExchangeInfo = {
                CmcResponse(CmcStatus(), mapOf("1" to ExchangeInfoDto(id = 1, name = "Exchange 1")))
            }

            repository.loadPage(page = 0, pageSize = 25)

            assertEquals(1, api.mapCallCount)
        }

    @Test
    fun `dado indice e lista validos, quando loadPage, entao nao dispara nenhuma chamada de rede`() =
        runTest {
            seedIndexAndExchange(id = 1, syncedAt = timeProvider.currentMillis, updatedAt = timeProvider.currentMillis)

            val result = repository.loadPage(page = 0, pageSize = 25)

            assertTrue(result.getOrNull() is PageLoad.Fresh)
            assertEquals(0, api.mapCallCount)
            assertEquals(0, api.infoCallCount)
        }

    @Test
    fun `dado erro de rede no info com cache existente, quando loadPage, entao retorna PageLoad Cached preservando os dados`() =
        runTest {
            val staleUpdatedAt = timeProvider.currentMillis - 10.minutes.inWholeMilliseconds
            seedIndexAndExchange(id = 1, syncedAt = timeProvider.currentMillis, updatedAt = staleUpdatedAt)
            val networkError = IOException("boom")
            api.onGetExchangeInfo = { throw networkError }

            val result = repository.loadPage(page = 0, pageSize = 25)

            val pageLoad = result.getOrNull()
            assertTrue(pageLoad is PageLoad.Cached)
            assertEquals(AppError.Unknown(networkError), (pageLoad as PageLoad.Cached).error)
            assertEquals(1, db.exchangeDao().getById(1)?.id)
        }

    @Test
    fun `dado erro de rede no info sem nenhum cache existente, quando loadPage, entao retorna failure`() =
        runTest {
            db.exchangeIndexDao().insertAll(
                listOf(ExchangeIndexEntity(id = 1, rank = 0, name = "Exchange 1", syncedAt = timeProvider.currentMillis)),
            )
            api.onGetExchangeInfo = { throw IOException("boom") }

            val result = repository.loadPage(page = 0, pageSize = 25)

            assertTrue(result.isFailure)
        }

    @Test
    fun `dado refresh, quando executado, entao invalida e repopula preservando a ordenacao por rank`() =
        runTest {
            seedIndexAndExchange(id = 1, syncedAt = timeProvider.currentMillis, updatedAt = timeProvider.currentMillis)
            seedIndexAndExchange(id = 2, syncedAt = timeProvider.currentMillis, updatedAt = timeProvider.currentMillis)
            api.onGetExchangeMap = {
                CmcResponse(
                    CmcStatus(),
                    listOf(
                        ExchangeMapItemDto(id = 2, name = "Exchange 2", slug = "exchange-2"),
                        ExchangeMapItemDto(id = 3, name = "Exchange 3", slug = "exchange-3"),
                    ),
                )
            }

            val result = repository.refresh()

            assertTrue(result.isSuccess)
            val orderedIds = db.exchangeIndexDao().idsForRange(0, 10)
            assertEquals(listOf(2, 3), orderedIds)
            assertEquals(null, db.exchangeDao().getById(1))
        }

    @Test
    fun `dado erro no map com indice ja existente, quando loadPage, entao degrada servindo o indice antigo`() =
        runTest {
            val staleSyncedAt = timeProvider.currentMillis - 20.minutes.inWholeMilliseconds
            seedIndexAndExchange(id = 1, syncedAt = staleSyncedAt, updatedAt = timeProvider.currentMillis)
            api.onGetExchangeMap = { throw IOException("boom") }

            val result = repository.loadPage(page = 0, pageSize = 25)

            assertTrue(result.isSuccess)
            assertEquals(0, api.infoCallCount)
        }

    @Test
    fun `dado detalhe em cache dentro do TTL, quando syncExchangeDetail, entao nao chama a API`() =
        runTest {
            db.exchangeDetailDao().upsert(
                ExchangeDetailEntity(
                    id = 1,
                    description = "desc",
                    websiteUrl = null,
                    makerFee = null,
                    takerFee = null,
                    updatedAt = timeProvider.currentMillis,
                ),
            )

            val result = repository.syncExchangeDetail(1)

            assertTrue(result.isSuccess)
            assertEquals(0, api.infoCallCount)
        }

    @Test
    fun `dado detalhe expirado, quando syncExchangeDetail, entao busca na API e atualiza o cache`() =
        runTest {
            val staleUpdatedAt = timeProvider.currentMillis - 25.hours.inWholeMilliseconds
            db.exchangeDetailDao().upsert(
                ExchangeDetailEntity(
                    id = 1,
                    description = "old",
                    websiteUrl = null,
                    makerFee = null,
                    takerFee = null,
                    updatedAt = staleUpdatedAt,
                ),
            )
            api.onGetExchangeInfo =
                { CmcResponse(CmcStatus(), mapOf("1" to ExchangeInfoDto(id = 1, name = "Exchange 1", description = "new"))) }

            val result = repository.syncExchangeDetail(1)

            assertTrue(result.isSuccess)
            assertEquals(1, api.infoCallCount)
            assertEquals("new", db.exchangeDetailDao().getById(1)?.description)
        }

    @Test
    fun `dado assets nunca sincronizados, quando getExchangeAssets, entao busca na API e persiste`() =
        runTest {
            api.onGetExchangeAssets = {
                CmcResponse(
                    CmcStatus(),
                    listOf(
                        ExchangeAssetDto(
                            walletAddress = "0xabc",
                            balance = 10.0,
                            currency = AssetCurrencyDto(symbol = "ETH", name = "Ethereum", priceUsd = 5.0),
                        ),
                    ),
                )
            }

            val result = repository.getExchangeAssets(1)

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull()?.size)
            assertEquals(1, api.assetsCallCount)
        }

    @Test
    fun `dado assets sincronizados e vazios, quando getExchangeAssets novamente dentro do TTL, entao nao chama a API de novo`() =
        runTest {
            api.onGetExchangeAssets = { CmcResponse(CmcStatus(), emptyList()) }

            repository.getExchangeAssets(1)
            val second = repository.getExchangeAssets(1)

            assertTrue(second.isSuccess)
            assertEquals(emptyList<Any>(), second.getOrNull())
            assertEquals(1, api.assetsCallCount)
        }

    @Test
    fun `dado header e detalhe em cache, quando observeExchangeDetail, entao combina as duas fontes`() =
        runTest {
            seedIndexAndExchange(id = 1, syncedAt = timeProvider.currentMillis, updatedAt = timeProvider.currentMillis)
            db.exchangeDetailDao().upsert(
                ExchangeDetailEntity(
                    id = 1,
                    description = "desc",
                    websiteUrl = "https://example.com",
                    makerFee = 0.001,
                    takerFee = 0.002,
                    updatedAt = timeProvider.currentMillis,
                ),
            )

            repository.observeExchangeDetail(1).test {
                val detail = awaitItem()
                assertEquals(1, detail?.id)
                assertEquals("desc", detail?.description)
                assertEquals("https://example.com", detail?.websiteUrl)
            }
        }

    @Test
    fun `dado erro de rede no map, quando refresh, entao retorna failure`() =
        runTest {
            val networkError = IOException("boom")
            api.onGetExchangeMap = { throw networkError }

            val result = repository.refresh()

            assertTrue(result.isFailure)
            assertEquals(AppError.Unknown(networkError), result.errorOrNull())
        }
}
