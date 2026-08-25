package br.com.edmundo.desafiomb.feature.exchanges.detail

import br.com.edmundo.desafiomb.core.ui.state.UiError
import br.com.edmundo.desafiomb.feature.exchanges.model.ExchangeAssetUiModel
import br.com.edmundo.desafiomb.feature.exchanges.model.ExchangeDetailUiModel

data class ExchangeDetailUiState(
    val isLoadingHeader: Boolean = true,
    val header: ExchangeDetailUiModel? = null,
    val headerError: UiError? = null,
    val assetsState: AssetsState = AssetsState.Loading,
)

sealed interface AssetsState {
    data object Loading : AssetsState

    data class Content(
        val items: List<ExchangeAssetUiModel>,
        val total: Int,
    ) : AssetsState

    data object Empty : AssetsState

    data class Error(
        val error: UiError,
    ) : AssetsState
}
