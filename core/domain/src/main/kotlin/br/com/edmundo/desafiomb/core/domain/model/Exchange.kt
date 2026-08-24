package br.com.edmundo.desafiomb.core.domain.model

import java.time.Instant

data class Exchange(
    val id: Int,
    val name: String,
    val logoUrl: String?,
    val spotVolumeUsd: Double?,
    val dateLaunched: Instant?,
)
