package br.com.edmundo.desafiomb.feature.exchanges.list

sealed interface ExchangeListEvent {
    data object LoadMore : ExchangeListEvent

    data object Refresh : ExchangeListEvent

    data object Retry : ExchangeListEvent

    data object RetryAppend : ExchangeListEvent
}
