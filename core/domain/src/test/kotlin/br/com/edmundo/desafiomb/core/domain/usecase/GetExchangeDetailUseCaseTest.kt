@file:OptIn(ExperimentalCoroutinesApi::class)

package br.com.edmundo.desafiomb.core.domain.usecase

import br.com.edmundo.desafiomb.core.domain.error.AppError
import br.com.edmundo.desafiomb.core.domain.model.Exchange
import br.com.edmundo.desafiomb.core.domain.model.ExchangeAsset
import br.com.edmundo.desafiomb.core.domain.model.ExchangeDetail
import br.com.edmundo.desafiomb.core.domain.model.PageLoad
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class DelayingFakeRepository(
    private val detailDelayMs: Long,
    private val assetsDelayMs: Long,
    private val detailResult: DomainResult<Unit>,
    private val assetsResult: DomainResult<List<ExchangeAsset>>,
) : ExchangeRepository {
    override fun observeExchanges(): Flow<List<Exchange>> = flowOf(emptyList())
    override suspend fun loadPage(page: Int, pageSize: Int): DomainResult<PageLoad> =
        throw NotImplementedError("nao utilizado neste teste")
    override suspend fun refresh(): DomainResult<Unit> = throw NotImplementedError("nao utilizado neste teste")
    override fun observeExchangeDetail(id: Int): Flow<ExchangeDetail?> = flowOf(null)
    override suspend fun syncExchangeDetail(id: Int): DomainResult<Unit> {
        delay(detailDelayMs)
        return detailResult
    }
    override suspend fun getExchangeAssets(id: Int): DomainResult<List<ExchangeAsset>> {
        delay(assetsDelayMs)
        return assetsResult
    }
}

class GetExchangeDetailUseCaseTest {

    @Test
    fun `dado info e assets com latencia igual, quando invocado, entao as chamadas rodam em paralelo`() = runTest {
        val repository = DelayingFakeRepository(
            detailDelayMs = 100,
            assetsDelayMs = 100,
            detailResult = DomainResult.success(Unit),
            assetsResult = DomainResult.success(emptyList()),
        )
        val useCase = GetExchangeDetailUseCase(repository)

        useCase(id = 1)

        assertEquals(100L, testScheduler.currentTime)
    }

    @Test
    fun `dado detail com falha e assets com sucesso, quando invocado, entao o bundle carrega os dois resultados independentes`() = runTest {
        val repository = DelayingFakeRepository(
            detailDelayMs = 0,
            assetsDelayMs = 0,
            detailResult = DomainResult.failure(AppError.NoConnection),
            assetsResult = DomainResult.success(emptyList()),
        )
        val useCase = GetExchangeDetailUseCase(repository)

        val bundle = useCase(id = 1)

        assertTrue(bundle.detailResult.isFailure)
        assertTrue(bundle.assetsResult.isSuccess)
    }

    @Test
    fun `dado detail com sucesso e assets com falha, quando invocado, entao o bundle carrega os dois resultados independentes`() = runTest {
        val repository = DelayingFakeRepository(
            detailDelayMs = 0,
            assetsDelayMs = 0,
            detailResult = DomainResult.success(Unit),
            assetsResult = DomainResult.failure(AppError.Server),
        )
        val useCase = GetExchangeDetailUseCase(repository)

        val bundle = useCase(id = 1)

        assertTrue(bundle.detailResult.isSuccess)
        assertTrue(bundle.assetsResult.isFailure)
    }
}
