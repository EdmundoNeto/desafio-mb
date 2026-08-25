package br.com.edmundo.desafiomb.core.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class TtlPolicyTest {
    @Test
    fun `dado updatedAt dentro do TTL, quando isStale, entao retorna false`() {
        val now = 100_000L
        val updatedAt = now - 1.minutes.inWholeMilliseconds
        assertFalse(TtlPolicy.isStale(updatedAt, ttl = 5.minutes, now = now))
    }

    @Test
    fun `dado updatedAt fora do TTL, quando isStale, entao retorna true`() {
        val now = 100_000L
        val updatedAt = now - 10.minutes.inWholeMilliseconds
        assertTrue(TtlPolicy.isStale(updatedAt, ttl = 5.minutes, now = now))
    }

    @Test
    fun `dado updatedAt exatamente no limite do TTL, quando isStale, entao retorna false`() {
        val now = 100_000L
        val updatedAt = now - 5.minutes.inWholeMilliseconds
        assertFalse(TtlPolicy.isStale(updatedAt, ttl = 5.minutes, now = now))
    }

    @Test
    fun `dado constantes de TTL, quando lidas, entao correspondem ao contrato da spec`() {
        assertEquals(15.minutes, TtlPolicy.INDEX)
        assertEquals(5.minutes, TtlPolicy.LIST)
        assertEquals(24.hours, TtlPolicy.DETAIL)
        assertEquals(5.minutes, TtlPolicy.ASSETS)
    }
}
