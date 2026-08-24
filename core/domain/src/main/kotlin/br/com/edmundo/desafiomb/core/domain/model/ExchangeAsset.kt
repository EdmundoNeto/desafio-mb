package br.com.edmundo.desafiomb.core.domain.model

data class ExchangeAsset(
    val walletAddress: String,
    val currencyName: String,
    val currencyPriceUsd: Double,
    val currencySymbol: String,
    val balance: Double,
)
