package br.com.edmundo.desafiomb.feature.exchanges.model

data class ExchangeUiModel(
    val id: Int,
    val name: String,
    val logoUrl: String?,
    val volume: String,
    val launchedAt: String,
)
