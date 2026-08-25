package br.com.edmundo.desafiomb.feature.exchanges.model

data class ExchangeDetailUiModel(
    val id: Int,
    val name: String,
    val logoUrl: String?,
    val description: String?,
    val websiteUrl: String?,
    val makerFee: String,
    val takerFee: String,
    val launchedAt: String,
)
