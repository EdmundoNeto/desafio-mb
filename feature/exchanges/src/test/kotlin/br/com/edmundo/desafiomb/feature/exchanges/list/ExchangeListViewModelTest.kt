@file:OptIn(ExperimentalCoroutinesApi::class)

package br.com.edmundo.desafiomb.feature.exchanges.list

import app.cash.turbine.test
import br.com.edmundo.desafiomb.core.domain.error.AppError
import br.com.edmundo.desafiomb.core.domain.model.PageLoad
import br.com.edmundo.desafiomb.core.domain.usecase.LoadExchangesPageUseCase
import br.com.edmundo.desafiomb.core.domain.usecase.ObserveExchangesUseCase
import br.com.edmundo.desafiomb.core.domain.usecase.RefreshExchangesUseCase
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import br.com.edmundo.desafiomb.core.testing.di.MainDispatcherRule
import br.com.edmundo.desafiomb.core.testing.fake.FakeExchangeRepository
import br.com.edmundo.desafiomb.core.testing.fixture.ExchangeFixtures
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExchangeListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeExchangeRepository()

    private fun viewModel() = ExchangeListViewModel(
        observeExchanges = ObserveExchangesUseCase(repository),
        loadExchangesPage = LoadExchangesPageUseCase(repository),
        refreshExchanges = RefreshExchangesUseCase(repository),
    )

    @Test
    fun `L01 dado init, quando viewmodel criado, entao isLoadingFirstPage comeca true`() = runTest {
        val vm = viewModel()
        assertTrue(vm.state.value.isLoadingFirstPage)
    }

    @Test
    fun `L02 dado cache emitindo item, quando coletado, entao items populado e isLoadingFirstPage false`() = runTest {
        val vm = viewModel()
        vm.state.test {
            awaitItem()
            repository.emit(ExchangeFixtures.exchangeList(1))
            advanceUntilIdle()
            val afterEmit = awaitItem()
            assertEquals(1, afterEmit.items.size)
            assertTrue(!afterEmit.isLoadingFirstPage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `L03 dado loadPage com sucesso fresh, quando carregado, entao limpa erro e isStale`() = runTest {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = true))
        val vm = viewModel()
        advanceUntilIdle()
        vm.state.test {
            val state = awaitItem()
            assertEquals(null, state.fullScreenError)
            assertTrue(!state.isStale)
            assertTrue(!state.isLoadingFirstPage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `L04 dado loadPage com falha e items vazio, quando carregado, entao fullScreenError preenchido`() = runTest {
        repository.loadPageResult = DomainResult.failure(AppError.NoConnection)
        val vm = viewModel()
        advanceUntilIdle()
        vm.state.test {
            assertTrue(awaitItem().fullScreenError != null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `L05 dado loadPage cached com erro e items nao vazio, quando carregado, entao isStale true sem fullScreenError`() = runTest {
        repository.emit(ExchangeFixtures.exchangeList(1))
        repository.loadPageResult = DomainResult.success(PageLoad.Cached(hasMore = false, error = AppError.NoConnection))
        val vm = viewModel()
        advanceUntilIdle()
        vm.state.test {
            val state = awaitItem()
            assertTrue(state.isStale)
            assertEquals(null, state.fullScreenError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `L06 dado appendState loading, quando onLoadMore chamado de novo, entao loadPage nao e chamado outra vez`() = runTest {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = true))
        val vm = viewModel()
        advanceUntilIdle()
        val gate = CompletableDeferred<Unit>()
        repository.loadPageGate = gate
        vm.onEvent(ExchangeListEvent.LoadMore)
        vm.onEvent(ExchangeListEvent.LoadMore)
        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(listOf(0, 1), repository.loadPageCalls)
    }

    @Test
    fun `L07 dado conteudo, quando onLoadMore, entao appendState fica loading e loadPage e chamado`() = runTest {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = true))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(ExchangeListEvent.LoadMore)
        advanceUntilIdle()
        assertTrue(repository.loadPageCalls.contains(1))
    }

    @Test
    fun `L08 dado append com sucesso e hasMore true, quando concluido, entao appendState idle e page avanca`() = runTest {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = true))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(ExchangeListEvent.LoadMore)
        advanceUntilIdle()
        vm.state.test {
            assertEquals(AppendState.Idle, awaitItem().appendState)
            cancelAndIgnoreRemainingEvents()
        }
        vm.onEvent(ExchangeListEvent.LoadMore)
        advanceUntilIdle()
        assertEquals(listOf(0, 1, 2), repository.loadPageCalls)
    }

    @Test
    fun `L09 dado append com sucesso e hasMore false, quando concluido, entao appendState endReached`() = runTest {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = false))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(ExchangeListEvent.LoadMore)
        advanceUntilIdle()
        vm.state.test {
            assertEquals(AppendState.EndReached, awaitItem().appendState)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `L10 dado append com falha, quando concluido, entao appendState error e page nao avanca`() = runTest {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = true))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(ExchangeListEvent.LoadMore)
        advanceUntilIdle()
        repository.loadPageResult = DomainResult.failure(AppError.Server)
        vm.onEvent(ExchangeListEvent.LoadMore)
        advanceUntilIdle()
        vm.state.test {
            assertTrue(awaitItem().appendState is AppendState.Error)
            cancelAndIgnoreRemainingEvents()
        }
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = false))
        vm.onEvent(ExchangeListEvent.LoadMore)
        advanceUntilIdle()
        assertEquals(listOf(0, 1, 2, 2), repository.loadPageCalls)
    }

    @Test
    fun `L11 dado qualquer estado, quando onRefresh, entao chama refresh e loadPage de pagina 0 e volta appendState idle`() = runTest {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = true))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(ExchangeListEvent.LoadMore)
        advanceUntilIdle()
        vm.onEvent(ExchangeListEvent.Refresh)
        advanceUntilIdle()
        assertEquals(1, repository.refreshCalls)
        vm.state.test {
            val state = awaitItem()
            assertEquals(AppendState.Idle, state.appendState)
            assertTrue(!state.isRefreshing)
            cancelAndIgnoreRemainingEvents()
        }
        vm.onEvent(ExchangeListEvent.LoadMore)
        advanceUntilIdle()
        assertEquals(listOf(0, 1, 0, 1), repository.loadPageCalls)
    }

    @Test
    fun `L12 dado erro full screen, quando onRetry, entao limpa erro e recarrega pagina 0`() = runTest {
        repository.loadPageResult = DomainResult.failure(AppError.NoConnection)
        val vm = viewModel()
        advanceUntilIdle()
        vm.state.test {
            assertTrue(awaitItem().fullScreenError != null)
            cancelAndIgnoreRemainingEvents()
        }
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = true))
        vm.onEvent(ExchangeListEvent.Retry)
        advanceUntilIdle()
        vm.state.test {
            assertEquals(null, awaitItem().fullScreenError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `L13 dado append error, quando onRetryAppend, entao tenta a mesma pagina de novo`() = runTest {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = true))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onEvent(ExchangeListEvent.LoadMore)
        advanceUntilIdle()
        repository.loadPageResult = DomainResult.failure(AppError.Server)
        vm.onEvent(ExchangeListEvent.LoadMore)
        advanceUntilIdle()
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = false))
        vm.onEvent(ExchangeListEvent.RetryAppend)
        advanceUntilIdle()
        vm.state.test {
            assertEquals(AppendState.EndReached, awaitItem().appendState)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(0, 1, 2, 2), repository.loadPageCalls)
    }
}
