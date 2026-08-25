package br.com.edmundo.desafiomb.core.testing.fake

import br.com.edmundo.desafiomb.core.domain.model.Exchange
import br.com.edmundo.desafiomb.core.domain.model.ExchangeAsset
import br.com.edmundo.desafiomb.core.domain.model.ExchangeDetail
import br.com.edmundo.desafiomb.core.domain.model.PageLoad
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeExchangeRepository : ExchangeRepository {
    private val exchangesFlow = MutableStateFlow<List<Exchange>>(emptyList())
    private val detailFlow = MutableStateFlow<ExchangeDetail?>(null)

    var loadPageResult: DomainResult<PageLoad> = DomainResult.success(PageLoad.Fresh(hasMore = false))
    var refreshResult: DomainResult<Unit> = DomainResult.success(Unit)
    var syncExchangeDetailResult: DomainResult<Unit> = DomainResult.success(Unit)
    var assetsResult: DomainResult<List<ExchangeAsset>> = DomainResult.success(emptyList())
    var loadPageGate: CompletableDeferred<Unit>? = null

    var loadPageCalls: List<Int> = emptyList()
        private set
    var refreshCalls: Int = 0
        private set
    var syncExchangeDetailCalls: List<Int> = emptyList()
        private set
    var getExchangeAssetsCalls: List<Int> = emptyList()
        private set

    fun emit(exchanges: List<Exchange>) {
        exchangesFlow.value = exchanges
    }

    fun emitDetail(detail: ExchangeDetail?) {
        detailFlow.value = detail
    }

    override fun observeExchanges(): Flow<List<Exchange>> = exchangesFlow

    override suspend fun loadPage(
        page: Int,
        pageSize: Int,
    ): DomainResult<PageLoad> {
        loadPageCalls = loadPageCalls + page
        loadPageGate?.await()
        return loadPageResult
    }

    override suspend fun refresh(): DomainResult<Unit> {
        refreshCalls += 1
        return refreshResult
    }

    override fun observeExchangeDetail(id: Int): Flow<ExchangeDetail?> = detailFlow

    override suspend fun syncExchangeDetail(id: Int): DomainResult<Unit> {
        syncExchangeDetailCalls = syncExchangeDetailCalls + id
        return syncExchangeDetailResult
    }

    override suspend fun getExchangeAssets(id: Int): DomainResult<List<ExchangeAsset>> {
        getExchangeAssetsCalls = getExchangeAssetsCalls + id
        return assetsResult
    }
}
