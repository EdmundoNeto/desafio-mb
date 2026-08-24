package br.com.edmundo.desafiomb.core.domain.error

import kotlin.time.Duration

/**
 * Erros de dominio (PRD 5.5). Nenhuma Exception generica cruza a fronteira do repositorio
 * (spec 3.4, invariante). O `when` sobre este tipo e exaustivo em compilacao.
 */
sealed interface AppError {
    data object NoConnection : AppError
    data object Timeout : AppError

    /** HTTP 401 / CMC 1001, 1002 - inclui o caso de CMC_API_KEY vazia (RNF-10, CA-10). */
    data object InvalidApiKey : AppError

    /** HTTP 403 / CMC 1006. */
    data object PlanNotAuthorized : AppError

    /** HTTP 429 / CMC 1008-1010. [retryAfter] vem do header `Retry-After` quando presente. */
    data class RateLimited(val retryAfter: Duration?) : AppError

    data object Server : AppError
    data object Serialization : AppError
    data class Unknown(val cause: Throwable?) : AppError
}
