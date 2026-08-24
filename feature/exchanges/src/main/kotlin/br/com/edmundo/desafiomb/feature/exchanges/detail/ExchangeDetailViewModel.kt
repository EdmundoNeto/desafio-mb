package br.com.edmundo.desafiomb.feature.exchanges.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import br.com.edmundo.desafiomb.core.domain.model.ExchangeAsset
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.usecase.GetExchangeAssetsUseCase
import br.com.edmundo.desafiomb.core.domain.usecase.GetExchangeDetailUseCase
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import br.com.edmundo.desafiomb.core.domain.util.fold
import br.com.edmundo.desafiomb.feature.exchanges.mapper.toUiError
import br.com.edmundo.desafiomb.feature.exchanges.mapper.toUiModel
import br.com.edmundo.desafiomb.feature.exchanges.navigation.ExchangeDetailRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExchangeDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ExchangeRepository,
    private val getExchangeDetail: GetExchangeDetailUseCase,
    private val getExchangeAssets: GetExchangeAssetsUseCase,
) : ViewModel() {

    private val exchangeId: Int = savedStateHandle.toRoute<ExchangeDetailRoute>().exchangeId

    private val _state = MutableStateFlow(ExchangeDetailUiState())
    val state: StateFlow<ExchangeDetailUiState> = _state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExchangeDetailUiState())

    private val _events = Channel<ExchangeDetailEvent>(Channel.BUFFERED)
    val events: Flow<ExchangeDetailEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.observeExchangeDetail(exchangeId).collect { detail ->
                if (detail != null) {
                    _state.update { it.copy(header = detail.toUiModel(), isLoadingHeader = false) }
                }
            }
        }
        loadDetail()
    }

    fun onRetryHeader() {
        _state.update { it.copy(headerError = null, isLoadingHeader = true) }
        loadDetail()
    }

    fun onRetryAssets() {
        _state.update { it.copy(assetsState = AssetsState.Loading) }
        viewModelScope.launch {
            val result = getExchangeAssets(exchangeId)
            _state.update { it.copy(assetsState = toAssetsState(result)) }
        }
    }

    fun onWebsiteClick() {
        val url = _state.value.header?.websiteUrl ?: return
        viewModelScope.launch { _events.send(ExchangeDetailEvent.OpenUrl(url)) }
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _state.update { it.copy(assetsState = AssetsState.Loading) }
            val bundle = getExchangeDetail(exchangeId)
            _state.update { current ->
                applyDetailResult(current, bundle.detailResult).copy(assetsState = toAssetsState(bundle.assetsResult))
            }
        }
    }

    private fun applyDetailResult(
        current: ExchangeDetailUiState,
        result: DomainResult<Unit>,
    ): ExchangeDetailUiState = result.fold(
        onSuccess = { current.copy(headerError = null, isLoadingHeader = false) },
        onFailure = { error ->
            if (current.header == null) {
                current.copy(headerError = error.toUiError(), isLoadingHeader = false)
            } else {
                current.copy(isLoadingHeader = false)
            }
        },
    )

    private fun toAssetsState(result: DomainResult<List<ExchangeAsset>>): AssetsState = result.fold(
        onSuccess = { assets ->
            if (assets.isEmpty()) {
                AssetsState.Empty
            } else {
                AssetsState.Content(items = assets.map { it.toUiModel() }, total = assets.size)
            }
        },
        onFailure = { error -> AssetsState.Error(error.toUiError()) },
    )
}
