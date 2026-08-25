package br.com.edmundo.desafiomb.core.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

private const val DEFAULT_PERMITS_PER_MINUTE = 50
private const val NANOS_PER_MINUTE = 60_000_000_000.0
private const val NANOS_PER_MILLI = 1_000_000L
private const val TOKENS_PER_REQUEST = 1.0
private const val MIN_WAIT_MILLIS = 1L

class RateLimitInterceptor(
    permitsPerMinute: Int = DEFAULT_PERMITS_PER_MINUTE,
    private val nanoTime: () -> Long = System::nanoTime,
    private val sleep: (Long) -> Unit = Thread::sleep,
) : Interceptor {
    private val capacity = permitsPerMinute.toDouble()
    private val refillPerNano = permitsPerMinute.toDouble() / NANOS_PER_MINUTE
    private val lock = Any()
    private var tokens = capacity
    private var lastRefill = nanoTime()

    override fun intercept(chain: Interceptor.Chain): Response {
        acquire()
        return chain.proceed(chain.request())
    }

    private fun acquire() {
        synchronized(lock) {
            refill()
            while (tokens < TOKENS_PER_REQUEST) {
                val waitNanos = ((TOKENS_PER_REQUEST - tokens) / refillPerNano).toLong()
                sleep((waitNanos / NANOS_PER_MILLI).coerceAtLeast(MIN_WAIT_MILLIS))
                refill()
            }
            tokens -= TOKENS_PER_REQUEST
        }
    }

    private fun refill() {
        val now = nanoTime()
        val elapsed = now - lastRefill
        if (elapsed > 0) {
            tokens = (tokens + elapsed * refillPerNano).coerceAtMost(capacity)
            lastRefill = now
        }
    }
}
