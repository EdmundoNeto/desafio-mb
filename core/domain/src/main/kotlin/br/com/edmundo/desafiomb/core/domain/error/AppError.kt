package br.com.edmundo.desafiomb.core.domain.error

import kotlin.time.Duration

sealed interface AppError {
    data object NoConnection : AppError

    data object Timeout : AppError

    data object InvalidApiKey : AppError

    data object PlanNotAuthorized : AppError

    data class RateLimited(
        val retryAfter: Duration?,
    ) : AppError

    data object Server : AppError

    data object Serialization : AppError

    data class Unknown(
        val cause: Throwable?,
    ) : AppError
}
