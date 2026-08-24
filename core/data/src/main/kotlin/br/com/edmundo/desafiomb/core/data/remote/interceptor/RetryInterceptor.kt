package br.com.edmundo.desafiomb.core.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.pow
import kotlin.random.Random

class RetryInterceptor(
    private val maxAttempts: Int = 3,
    private val sleep: (Long) -> Unit = Thread::sleep,
    private val random: Random = Random,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 1
        while (true) {
            val response = try {
                chain.proceed(chain.request())
            } catch (e: IOException) {
                if (attempt >= maxAttempts) throw e
                sleep(backoffMillis(attempt, retryAfterHeader = null))
                attempt++
                continue
            }

            if (!shouldRetry(response.code) || attempt >= maxAttempts) return response

            val retryAfter = response.header("Retry-After")
            response.close()
            sleep(backoffMillis(attempt, retryAfter))
            attempt++
        }
    }

    private fun shouldRetry(code: Int) = code == 408 || code == 429 || code in 500..599

    private fun backoffMillis(attempt: Int, retryAfterHeader: String?): Long {
        val retryAfterSeconds = retryAfterHeader?.toLongOrNull()
        if (retryAfterSeconds != null) return retryAfterSeconds * 1000

        val base = (2.0.pow(attempt) * 500).toLong().coerceAtMost(8000)
        val jitter = (base * 0.2 * (random.nextDouble() * 2 - 1)).toLong()
        return (base + jitter).coerceIn(1, 8000)
    }
}
