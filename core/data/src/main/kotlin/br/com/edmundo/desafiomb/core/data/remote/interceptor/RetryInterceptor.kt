package br.com.edmundo.desafiomb.core.data.remote.interceptor

import br.com.edmundo.desafiomb.core.data.remote.HttpHeaders
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.pow
import kotlin.random.Random

private const val BASE_BACKOFF_MILLIS = 500
private const val MAX_BACKOFF_MILLIS = 8000L
private const val MIN_BACKOFF_MILLIS = 1L
private const val JITTER_FACTOR = 0.2
private const val MILLIS_PER_SECOND = 1000L
private const val RETRYABLE_HTTP_REQUEST_TIMEOUT = 408
private const val RETRYABLE_HTTP_TOO_MANY_REQUESTS = 429
private val retryableHttpServerErrors = 500..599

class RetryInterceptor(
    private val maxAttempts: Int = 3,
    private val sleep: (Long) -> Unit = Thread::sleep,
    private val random: Random = Random,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 1
        while (true) {
            val response =
                try {
                    chain.proceed(chain.request())
                } catch (e: IOException) {
                    if (attempt >= maxAttempts) throw e
                    sleep(backoffMillis(attempt, retryAfterHeader = null))
                    attempt++
                    continue
                }

            if (!shouldRetry(response.code) || attempt >= maxAttempts) return response

            val retryAfter = response.header(HttpHeaders.RETRY_AFTER)
            response.close()
            sleep(backoffMillis(attempt, retryAfter))
            attempt++
        }
    }

    private fun shouldRetry(code: Int) =
        code == RETRYABLE_HTTP_REQUEST_TIMEOUT || code == RETRYABLE_HTTP_TOO_MANY_REQUESTS || code in retryableHttpServerErrors

    private fun backoffMillis(
        attempt: Int,
        retryAfterHeader: String?,
    ): Long {
        val retryAfterSeconds = retryAfterHeader?.toLongOrNull()
        if (retryAfterSeconds != null) return retryAfterSeconds * MILLIS_PER_SECOND

        val base = (2.0.pow(attempt) * BASE_BACKOFF_MILLIS).toLong().coerceAtMost(MAX_BACKOFF_MILLIS)
        val jitter = (base * JITTER_FACTOR * (random.nextDouble() * 2 - 1)).toLong()
        return (base + jitter).coerceIn(MIN_BACKOFF_MILLIS, MAX_BACKOFF_MILLIS)
    }
}
