package br.com.edmundo.desafiomb.core.testing.fake

import br.com.edmundo.desafiomb.core.domain.util.TimeProvider

class FakeTimeProvider(
    var currentMillis: Long = 0L,
) : TimeProvider {
    override fun now(): Long = currentMillis
}
