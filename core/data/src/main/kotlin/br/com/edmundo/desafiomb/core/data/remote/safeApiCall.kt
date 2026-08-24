package br.com.edmundo.desafiomb.core.data.remote

import br.com.edmundo.desafiomb.core.data.mapper.mapCmcErrorCode
import br.com.edmundo.desafiomb.core.data.mapper.mapThrowable
import br.com.edmundo.desafiomb.core.data.remote.dto.CmcResponse
import br.com.edmundo.desafiomb.core.domain.error.AppError
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import kotlinx.coroutines.CancellationException

suspend fun <T, R> safeApiCall(
    call: suspend () -> CmcResponse<T>,
    transform: (T) -> R,
): DomainResult<R> = try {
    val response = call()
    when {
        response.status.errorCode != 0 -> DomainResult.failure(mapCmcErrorCode(response.status))
        response.data == null -> DomainResult.failure(AppError.Serialization)
        else -> DomainResult.success(transform(response.data))
    }
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    DomainResult.failure(mapThrowable(e))
}
