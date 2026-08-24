package br.com.edmundo.desafiomb.core.data.util

import br.com.edmundo.desafiomb.core.domain.util.TimeProvider

class SystemTimeProvider : TimeProvider {
    override fun now(): Long = System.currentTimeMillis()
}
