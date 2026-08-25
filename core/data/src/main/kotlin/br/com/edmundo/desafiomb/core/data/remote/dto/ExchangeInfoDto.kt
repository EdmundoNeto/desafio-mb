package br.com.edmundo.desafiomb.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeInfoDto(
    val id: Int,
    val name: String,
    val slug: String? = null,
    val logo: String? = null,
    val description: String? = null,
    @SerialName("date_launched") val dateLaunched: String? = null,
    @SerialName("maker_fee") val makerFee: Double? = null,
    @SerialName("taker_fee") val takerFee: Double? = null,
    @SerialName("spot_volume_usd") val spotVolumeUsd: Double? = null,
    val urls: ExchangeUrlsDto? = null,
)

@Serializable
data class ExchangeUrlsDto(
    val website: List<String> = emptyList(),
    val fee: List<String> = emptyList(),
    val twitter: List<String> = emptyList(),
)
