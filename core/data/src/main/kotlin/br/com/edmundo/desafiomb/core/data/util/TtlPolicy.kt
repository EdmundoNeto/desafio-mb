package br.com.edmundo.desafiomb.core.data.util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object TtlPolicy {
    val INDEX = 15.minutes
    val LIST = 5.minutes
    val DETAIL = 24.hours
    val ASSETS = 5.minutes

    fun isStale(
        updatedAt: Long,
        ttl: Duration,
        now: Long,
    ): Boolean = now - updatedAt > ttl.inWholeMilliseconds
}
