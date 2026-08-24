package br.com.edmundo.desafiomb.core.data.mapper

import br.com.edmundo.desafiomb.core.data.remote.dto.CmcStatus
import br.com.edmundo.desafiomb.core.domain.error.AppError
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.seconds

fun mapCmcErrorCode(status: CmcStatus): AppError = when (status.errorCode) {
    1001, 1002 -> AppError.InvalidApiKey
    1006 -> AppError.PlanNotAuthorized
    1008, 1009, 1010 -> AppError.RateLimited(retryAfter = null)
    else -> AppError.Unknown(cause = null)
}

fun mapThrowable(throwable: Throwable): AppError = when (throwable) {
    is UnknownHostException, is ConnectException -> AppError.NoConnection
    is SocketTimeoutException, is InterruptedIOException -> AppError.Timeout
    is SerializationException -> AppError.Serialization
    is HttpException -> mapHttpException(throwable)
    else -> AppError.Unknown(throwable)
}

private fun mapHttpException(exception: HttpException): AppError = when (exception.code()) {
    401 -> AppError.InvalidApiKey
    403 -> AppError.PlanNotAuthorized
    429 -> AppError.RateLimited(parseRetryAfter(exception.response()?.headers()?.get("Retry-After")))
    in 500..599 -> AppError.Server
    else -> AppError.Unknown(exception)
}

fun parseRetryAfter(headerValue: String?): kotlin.time.Duration? =
    headerValue?.toLongOrNull()?.seconds
