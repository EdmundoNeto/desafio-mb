package br.com.edmundo.desafiomb.feature.exchanges.navigation

import kotlinx.serialization.Serializable

@Serializable
data object ExchangeListRoute

@Serializable
data class ExchangeDetailRoute(
    val exchangeId: Int,
)
