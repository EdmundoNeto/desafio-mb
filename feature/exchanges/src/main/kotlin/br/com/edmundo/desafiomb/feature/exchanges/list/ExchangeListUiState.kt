package br.com.edmundo.desafiomb.feature.exchanges.list

import br.com.edmundo.desafiomb.core.ui.state.UiError
import br.com.edmundo.desafiomb.feature.exchanges.model.ExchangeUiModel

data class ExchangeListUiState(
    val items: List<ExchangeUiModel> = emptyList(),
    val isLoadingFirstPage: Boolean = true,
    val isRefreshing: Boolean = false,
    val appendState: AppendState = AppendState.Idle,
    val fullScreenError: UiError? = null,
    val isStale: Boolean = false,
)

sealed interface AppendState {
    data object Idle : AppendState

    data object Loading : AppendState

    data class Error(
        val error: UiError,
    ) : AppendState

    data object EndReached : AppendState
}
