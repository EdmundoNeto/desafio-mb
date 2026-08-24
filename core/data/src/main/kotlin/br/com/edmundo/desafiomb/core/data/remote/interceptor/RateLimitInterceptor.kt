package br.com.edmundo.desafiomb.core.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class RateLimitInterceptor(
    permitsPerMinute: Int = 50,
    private val nanoTime: () -> Long = System::nanoTime,
    private val sleep: (Long) -> Unit = Thread::sleep,
) : Interceptor {

    private val capacity = permitsPerMinute.toDouble()
    private val refillPerNano = permitsPerMinute.toDouble() / 60_000_000_000.0
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
            while (tokens < 1.0) {
                val waitNanos = ((1.0 - tokens) / refillPerNano).toLong()
                sleep((waitNanos / 1_000_000).coerceAtLeast(1))
                refill()
            }
            tokens -= 1.0
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
