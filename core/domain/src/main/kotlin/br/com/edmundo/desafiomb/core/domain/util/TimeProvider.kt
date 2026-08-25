package br.com.edmundo.desafiomb.core.domain.util

fun interface TimeProvider {
    fun now(): Long
}
