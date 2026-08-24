@file:OptIn(ExperimentalCoroutinesApi::class)

package br.com.edmundo.desafiomb.feature.exchanges.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import br.com.edmundo.desafiomb.core.domain.error.AppError
import br.com.edmundo.desafiomb.core.domain.usecase.GetExchangeAssetsUseCase
import br.com.edmundo.desafiomb.core.domain.usecase.GetExchangeDetailUseCase
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import br.com.edmundo.desafiomb.core.testing.di.MainDispatcherRule
import br.com.edmundo.desafiomb.core.testing.fake.FakeExchangeRepository
import br.com.edmundo.desafiomb.core.testing.fixture.ExchangeFixtures
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExchangeDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeExchangeRepository()

    private fun viewModel(exchangeId: Int = 270) = ExchangeDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("exchangeId" to exchangeId)),
        repository = repository,
        getExchangeDetail = GetExchangeDetailUseCase(repository),
        getExchangeAssets = GetExchangeAssetsUseCase(repository),
    )

    @Test
    fun `D01 dado init, quando viewmodel criado, entao isLoadingHeader e assetsState comecam loading`() = runTest {
        val vm = viewModel()
        assertTrue(vm.state.value.isLoadingHeader)
        assertEquals(AssetsState.Loading, vm.state.value.assetsState)
    }

    @Test
    fun `D02 dado cache de detalhe emitindo, quando coletado, entao header populado e isLoadingHeader false`() = runTest {
        val vm = viewModel()
        vm.state.test {
            awaitItem()
            repository.emitDetail(ExchangeFixtures.exchangeDetail())
            advanceUntilIdle()
            val afterEmit = awaitItem()
            assertEquals("Binance", afterEmit.header?.name)
            assertTrue(!afterEmit.isLoadingHeader)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `D03 dado detailResult com sucesso, quando carregado, entao headerError fica limpo`() = runTest {
        repository.syncExchangeDetailResult = DomainResult.success(Unit)
        val vm = viewModel()
        advanceUntilIdle()
        vm.state.test {
            assertNull(awaitItem().headerError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `D04 dado detailResult com falha e header nulo, quando carregado, entao headerError preenchido`() = runTest {
        repository.syncExchangeDetailResult = DomainResult.failure(AppError.NoConnection)
        val vm = viewModel()
        advanceUntilIdle()
        vm.state.test {
            val state = awaitItem()
            assertTrue(state.headerError != null)
            assertTrue(!state.isLoadingHeader)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `D05 dado detailResult com falha e header do cache presente, quando carregado, entao mantem header sem erro`() = runTest {
        repository.emitDetail(ExchangeFixtures.exchangeDetail())
        repository.syncExchangeDetailResult = DomainResult.failure(AppError.NoConnection)
        val vm = viewModel()
        advanceUntilIdle()
        vm.state.test {
            val state = awaitItem()
            assertNull(state.headerError)
            assertEquals("Binance", state.header?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `D06 dado assetsResult com sucesso nao vazio, quando carregado, entao assetsState content ordenado com total`() = runTest {
        repository.assetsResult = DomainResult.success(ExchangeFixtures.exchangeAssetList(2))
        val vm = viewModel()
        advanceUntilIdle()
        vm.state.test {
            val assetsState = awaitItem().assetsState
            assertTrue(assetsState is AssetsState.Content)
            assertEquals(2, (assetsState as AssetsState.Content).total)
            assertEquals(2, assetsState.items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `D07 dado assetsResult com sucesso vazio, quando carregado, entao assetsState empty`() = runTest {
        repository.assetsResult = DomainResult.success(emptyList())
        val vm = viewModel()
        advanceUntilIdle()
        vm.state.test {
            assertEquals(AssetsState.Empty, awaitItem().assetsState)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `D08 dado assetsResult com falha, quando carregado, entao assetsState error e header permanece`() = runTest {
        repository.emitDetail(ExchangeFixtures.exchangeDetail())
        repository.syncExchangeDetailResult = DomainResult.success(Unit)
        repository.assetsResult = DomainResult.failure(AppError.Server)
        val vm = viewModel()
        advanceUntilIdle()
        vm.state.test {
            val state = awaitItem()
            assertTrue(state.assetsState is AssetsState.Error)
            assertEquals("Binance", state.header?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `D09 dado onRetryAssets, quando chamado, entao recarrega apenas os assets`() = runTest {
        repository.assetsResult = DomainResult.failure(AppError.Server)
        val vm = viewModel()
        advanceUntilIdle()
        vm.state.test {
            assertTrue(awaitItem().assetsState is AssetsState.Error)
            cancelAndIgnoreRemainingEvents()
        }

        repository.assetsResult = DomainResult.success(ExchangeFixtures.exchangeAssetList(1))
        vm.onRetryAssets()
        advanceUntilIdle()

        vm.state.test {
            assertTrue(awaitItem().assetsState is AssetsState.Content)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(270, 270), repository.getExchangeAssetsCalls)
    }

    @Test
    fun `D10 dado header com websiteUrl, quando onWebsiteClick, entao emite OpenUrl`() = runTest {
        repository.emitDetail(ExchangeFixtures.exchangeDetail(websiteUrl = "https://binance.com"))
        val vm = viewModel()
        advanceUntilIdle()
        vm.events.test {
            vm.onWebsiteClick()
            val event = awaitItem()
            assertEquals(ExchangeDetailEvent.OpenUrl("https://binance.com"), event)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `D11 dado headerError preenchido, quando onRetryHeader, entao limpa erro e recarrega`() = runTest {
        repository.syncExchangeDetailResult = DomainResult.failure(AppError.NoConnection)
        val vm = viewModel()
        advanceUntilIdle()
        vm.state.test {
            assertTrue(awaitItem().headerError != null)
            cancelAndIgnoreRemainingEvents()
        }

        repository.syncExchangeDetailResult = DomainResult.success(Unit)
        repository.emitDetail(ExchangeFixtures.exchangeDetail())
        vm.onRetryHeader()
        advanceUntilIdle()

        vm.state.test {
            val state = awaitItem()
            assertNull(state.headerError)
            assertEquals("Binance", state.header?.name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
