package br.com.edmundo.desafiomb.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeMapItemDto(
    val id: Int,
    val name: String,
    val slug: String,
    @SerialName("is_active") val isActive: Int? = null,
)
