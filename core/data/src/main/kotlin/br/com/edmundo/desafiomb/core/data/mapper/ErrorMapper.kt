package br.com.edmundo.desafiomb.core.data.mapper

import br.com.edmundo.desafiomb.core.data.remote.HEADER_RETRY_AFTER
import br.com.edmundo.desafiomb.core.data.remote.dto.CmcStatus
import br.com.edmundo.desafiomb.core.domain.error.AppError
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.seconds

private const val CMC_ERROR_INVALID_API_KEY_MISSING = 1001
private const val CMC_ERROR_INVALID_API_KEY_WRONG = 1002
private const val CMC_ERROR_PLAN_NOT_AUTHORIZED = 1006
private const val CMC_ERROR_RATE_LIMIT_MINUTE = 1008
private const val CMC_ERROR_RATE_LIMIT_DAY = 1009
private const val CMC_ERROR_RATE_LIMIT_MONTH = 1010
private const val HTTP_TOO_MANY_REQUESTS = 429
private val httpServerErrorRange = HttpURLConnection.HTTP_INTERNAL_ERROR..599

fun mapCmcErrorCode(status: CmcStatus): AppError =
    when (status.errorCode) {
        CMC_ERROR_INVALID_API_KEY_MISSING, CMC_ERROR_INVALID_API_KEY_WRONG -> AppError.InvalidApiKey
        CMC_ERROR_PLAN_NOT_AUTHORIZED -> AppError.PlanNotAuthorized
        CMC_ERROR_RATE_LIMIT_MINUTE, CMC_ERROR_RATE_LIMIT_DAY, CMC_ERROR_RATE_LIMIT_MONTH -> AppError.RateLimited(retryAfter = null)
        else -> AppError.Unknown(cause = null)
    }

fun mapThrowable(throwable: Throwable): AppError =
    when (throwable) {
        is UnknownHostException, is ConnectException -> AppError.NoConnection
        is SocketTimeoutException, is InterruptedIOException -> AppError.Timeout
        is SerializationException -> AppError.Serialization
        is HttpException -> mapHttpException(throwable)
        else -> AppError.Unknown(throwable)
    }

private fun mapHttpException(exception: HttpException): AppError =
    when (exception.code()) {
        HttpURLConnection.HTTP_UNAUTHORIZED -> AppError.InvalidApiKey
        HttpURLConnection.HTTP_FORBIDDEN -> AppError.PlanNotAuthorized
        HTTP_TOO_MANY_REQUESTS -> AppError.RateLimited(parseRetryAfter(exception.response()?.headers()?.get(HEADER_RETRY_AFTER)))
        in httpServerErrorRange -> AppError.Server
        else -> AppError.Unknown(exception)
    }

fun parseRetryAfter(headerValue: String?): kotlin.time.Duration? = headerValue?.toLongOrNull()?.seconds
