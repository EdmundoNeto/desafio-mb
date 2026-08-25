package br.com.edmundo.desafiomb.feature.exchanges.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.edmundo.desafiomb.core.domain.model.PageLoad
import br.com.edmundo.desafiomb.core.domain.repository.UI_PAGE_SIZE
import br.com.edmundo.desafiomb.core.domain.usecase.LoadExchangesPageUseCase
import br.com.edmundo.desafiomb.core.domain.usecase.ObserveExchangesUseCase
import br.com.edmundo.desafiomb.core.domain.usecase.RefreshExchangesUseCase
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import br.com.edmundo.desafiomb.feature.exchanges.mapper.toUiError
import br.com.edmundo.desafiomb.feature.exchanges.mapper.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ExchangeListViewModel(
    observeExchanges: ObserveExchangesUseCase,
    private val loadExchangesPage: LoadExchangesPageUseCase,
    private val refreshExchanges: RefreshExchangesUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(ExchangeListUiState())
    val state: StateFlow<ExchangeListUiState> =
        _state
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExchangeListUiState())

    private val appendMutex = Mutex()
    private var page = 0

    init {
        viewModelScope.launch {
            observeExchanges().collect { exchanges ->
                _state.update { current ->
                    current.copy(
                        items = exchanges.map { it.toUiModel() },
                        isLoadingFirstPage = if (exchanges.isNotEmpty()) false else current.isLoadingFirstPage,
                    )
                }
            }
        }
        loadFirstPage()
    }

    fun onEvent(event: ExchangeListEvent) {
        when (event) {
            ExchangeListEvent.LoadMore -> onLoadMore()
            ExchangeListEvent.Refresh -> onRefresh()
            ExchangeListEvent.Retry -> onRetry()
            ExchangeListEvent.RetryAppend -> onLoadMore()
        }
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            val result = loadExchangesPage(0, UI_PAGE_SIZE)
            page = 1
            _state.update { applyFirstPageResult(it, result).copy(isLoadingFirstPage = false) }
        }
    }

    private fun onRetry() {
        _state.update { it.copy(fullScreenError = null, isLoadingFirstPage = true) }
        page = 0
        loadFirstPage()
    }

    private fun onRefresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            refreshExchanges()
            val result = loadExchangesPage(0, UI_PAGE_SIZE)
            page = 1
            _state.update { applyFirstPageResult(it, result).copy(isRefreshing = false, appendState = AppendState.Idle) }
        }
    }

    private fun applyFirstPageResult(
        current: ExchangeListUiState,
        result: DomainResult<PageLoad>,
    ): ExchangeListUiState =
        when (result) {
            is DomainResult.Success ->
                when (result.value) {
                    is PageLoad.Fresh -> current.copy(fullScreenError = null, isStale = false)
                    is PageLoad.Cached -> current.copy(fullScreenError = null, isStale = true)
                }

            is DomainResult.Failure ->
                if (current.items.isEmpty()) {
                    current.copy(fullScreenError = result.error.toUiError())
                } else {
                    current.copy(isStale = true)
                }
        }

    private fun onLoadMore() {
        if (_state.value.appendState is AppendState.Loading) return
        viewModelScope.launch {
            appendMutex.withLock {
                if (_state.value.appendState is AppendState.Loading) return@withLock
                _state.update { it.copy(appendState = AppendState.Loading) }
                val pageToLoad = page
                val result = loadExchangesPage(pageToLoad, UI_PAGE_SIZE)
                when (result) {
                    is DomainResult.Success -> {
                        val hasMore = result.value.hasMore
                        val isStale = result.value is PageLoad.Cached
                        page = pageToLoad + 1
                        _state.update { current ->
                            current.copy(
                                appendState = if (hasMore) AppendState.Idle else AppendState.EndReached,
                                isStale = current.isStale || isStale,
                            )
                        }
                    }

                    is DomainResult.Failure ->
                        _state.update {
                            it.copy(appendState = AppendState.Error(result.error.toUiError()))
                        }
                }
            }
        }
    }
}
