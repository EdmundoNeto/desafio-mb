package br.com.edmundo.desafiomb.core.domain.model

import java.time.Instant

data class ExchangeDetail(
    val id: Int,
    val name: String,
    val logoUrl: String?,
    val description: String?,
    val websiteUrl: String?,
    val makerFee: Double?,
    val takerFee: Double?,
    val dateLaunched: Instant?,
)
