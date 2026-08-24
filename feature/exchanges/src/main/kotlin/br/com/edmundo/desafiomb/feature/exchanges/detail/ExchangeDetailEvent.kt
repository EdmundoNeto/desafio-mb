package br.com.edmundo.desafiomb.feature.exchanges.detail

sealed interface ExchangeDetailEvent {
    data class OpenUrl(val url: String) : ExchangeDetailEvent
}
