package br.com.edmundo.desafiomb.core.domain.model

import br.com.edmundo.desafiomb.core.domain.error.AppError

sealed interface PageLoad {

    val hasMore: Boolean

    data class Fresh(override val hasMore: Boolean) : PageLoad

    data class Cached(override val hasMore: Boolean, val error: AppError) : PageLoad
}
