package br.com.edmundo.desafiomb.core.domain.usecase

import br.com.edmundo.desafiomb.core.domain.error.AppError
import br.com.edmundo.desafiomb.core.domain.model.Exchange
import br.com.edmundo.desafiomb.core.domain.model.ExchangeAsset
import br.com.edmundo.desafiomb.core.domain.model.ExchangeDetail
import br.com.edmundo.desafiomb.core.domain.model.PageLoad
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class RecordingFakeRepository : ExchangeRepository {
    var loadPageCall: Pair<Int, Int>? = null
    var refreshCalled = false
    var getExchangeAssetsCall: Int? = null

    var loadPageResult: DomainResult<PageLoad> = DomainResult.success(PageLoad.Fresh(hasMore = true))
    var refreshResult: DomainResult<Unit> = DomainResult.success(Unit)
    var assetsResult: DomainResult<List<ExchangeAsset>> = DomainResult.success(emptyList())
    var exchanges: List<Exchange> = emptyList()

    override fun observeExchanges(): Flow<List<Exchange>> = flowOf(exchanges)

    override suspend fun loadPage(
        page: Int,
        pageSize: Int,
    ): DomainResult<PageLoad> {
        loadPageCall = page to pageSize
        return loadPageResult
    }

    override suspend fun refresh(): DomainResult<Unit> {
        refreshCalled = true
        return refreshResult
    }

    override fun observeExchangeDetail(id: Int): Flow<ExchangeDetail?> = flowOf(null)

    override suspend fun syncExchangeDetail(id: Int): DomainResult<Unit> = throw NotImplementedError("nao utilizado neste teste")

    override suspend fun getExchangeAssets(id: Int): DomainResult<List<ExchangeAsset>> {
        getExchangeAssetsCall = id
        return assetsResult
    }
}

class ThinUseCasesTest {
    @Test
    fun `dado ObserveExchangesUseCase, quando invocado, entao delega para observeExchanges`() =
        runTest {
            val exchange = Exchange(id = 1, name = "Binance", logoUrl = null, spotVolumeUsd = null, dateLaunched = null)
            val repository = RecordingFakeRepository().apply { exchanges = listOf(exchange) }
            val useCase = ObserveExchangesUseCase(repository)

            val result =
                useCase().let { flow ->
                    var last: List<Exchange> = emptyList()
                    flow.collect { last = it }
                    last
                }

            assertEquals(listOf(exchange), result)
        }

    @Test
    fun `dado LoadExchangesPageUseCase, quando invocado sem pageSize, entao delega com UI_PAGE_SIZE`() =
        runTest {
            val repository = RecordingFakeRepository()
            val useCase = LoadExchangesPageUseCase(repository)

            val result = useCase(page = 2)

            assertEquals(2 to 25, repository.loadPageCall)
            assertEquals(repository.loadPageResult, result)
        }

    @Test
    fun `dado RefreshExchangesUseCase, quando invocado, entao delega para refresh`() =
        runTest {
            val repository = RecordingFakeRepository()
            val useCase = RefreshExchangesUseCase(repository)

            val result = useCase()

            assertEquals(true, repository.refreshCalled)
            assertEquals(repository.refreshResult, result)
        }

    @Test
    fun `dado GetExchangeAssetsUseCase, quando invocado, entao delega para getExchangeAssets com o id`() =
        runTest {
            val repository = RecordingFakeRepository().apply { assetsResult = DomainResult.failure(AppError.Server) }
            val useCase = GetExchangeAssetsUseCase(repository)

            val result = useCase(id = 270)

            assertEquals(270, repository.getExchangeAssetsCall)
            assertEquals(repository.assetsResult, result)
        }
}
