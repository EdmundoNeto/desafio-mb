package br.com.edmundo.desafiomb.core.domain.util

import br.com.edmundo.desafiomb.core.domain.error.AppError

sealed interface DomainResult<out T> {

    data class Success<out T>(val value: T) : DomainResult<T>

    data class Failure(val error: AppError) : DomainResult<Nothing>

    val isSuccess: Boolean get() = this is Success

    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.value

    fun errorOrNull(): AppError? = (this as? Failure)?.error

    companion object {
        fun <T> success(value: T): DomainResult<T> = Success(value)
        fun failure(error: AppError): DomainResult<Nothing> = Failure(error)
    }
}

inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> = when (this) {
    is DomainResult.Success -> DomainResult.Success(transform(value))
    is DomainResult.Failure -> this
}

inline fun <T, R> DomainResult<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (AppError) -> R,
): R = when (this) {
    is DomainResult.Success -> onSuccess(value)
    is DomainResult.Failure -> onFailure(error)
}

inline fun <T> DomainResult<T>.onSuccess(action: (T) -> Unit): DomainResult<T> = apply {
    if (this is DomainResult.Success) action(value)
}

inline fun <T> DomainResult<T>.onFailure(action: (AppError) -> Unit): DomainResult<T> = apply {
    if (this is DomainResult.Failure) action(error)
}
