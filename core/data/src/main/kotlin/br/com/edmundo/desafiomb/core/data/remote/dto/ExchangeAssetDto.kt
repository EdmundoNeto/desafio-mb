package br.com.edmundo.desafiomb.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeAssetDto(
    @SerialName("wallet_address") val walletAddress: String,
    val balance: Double? = null,
    val platform: AssetPlatformDto? = null,
    val currency: AssetCurrencyDto,
)

@Serializable
data class AssetPlatformDto(
    @SerialName("crypto_id") val cryptoId: Int? = null,
    val symbol: String? = null,
    val name: String? = null,
)

@Serializable
data class AssetCurrencyDto(
    @SerialName("crypto_id") val cryptoId: Int? = null,
    @SerialName("price_usd") val priceUsd: Double? = null,
    val symbol: String? = null,
    val name: String? = null,
)
