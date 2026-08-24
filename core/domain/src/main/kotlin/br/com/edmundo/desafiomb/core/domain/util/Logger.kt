package br.com.edmundo.desafiomb.core.domain.util

/**
 * Logging sem acoplar o dominio ao Android (RNF-08).
 * Implementado em :app por AndroidLogger, no-op em release.
 */
interface Logger {
    fun d(tag: String, message: String, throwable: Throwable? = null)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
