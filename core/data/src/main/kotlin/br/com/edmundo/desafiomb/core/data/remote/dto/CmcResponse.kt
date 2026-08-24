package br.com.edmundo.desafiomb.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CmcResponse<T>(
    val status: CmcStatus,
    val data: T? = null,
)

@Serializable
data class CmcStatus(
    @SerialName("error_code") val errorCode: Int = 0,
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("credit_count") val creditCount: Int = 0,
    val elapsed: Int = 0,
)
