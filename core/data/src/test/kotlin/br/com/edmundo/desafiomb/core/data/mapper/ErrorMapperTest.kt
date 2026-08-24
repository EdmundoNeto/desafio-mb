package br.com.edmundo.desafiomb.core.data.mapper

import br.com.edmundo.desafiomb.core.data.remote.dto.CmcStatus
import br.com.edmundo.desafiomb.core.domain.error.AppError
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.seconds

class ErrorMapperTest {

    private fun httpException(code: Int, headers: Map<String, String> = emptyMap()): HttpException {
        var builder = okhttp3.Headers.Builder()
        headers.forEach { (name, value) -> builder = builder.add(name, value) }
        val response = Response.error<Any>(
            "".toResponseBody("application/json".toMediaType()),
            okhttp3.Response.Builder()
                .code(code)
                .message("error")
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .request(okhttp3.Request.Builder().url("https://pro-api.coinmarketcap.com/").build())
                .headers(builder.build())
                .build(),
        )
        return HttpException(response)
    }

    @Test
    fun `dado UnknownHostException, quando mapeado, entao retorna NoConnection`() {
        assertEquals(AppError.NoConnection, mapThrowable(UnknownHostException()))
    }

    @Test
    fun `dado ConnectException, quando mapeado, entao retorna NoConnection`() {
        assertEquals(AppError.NoConnection, mapThrowable(ConnectException()))
    }

    @Test
    fun `dado SocketTimeoutException, quando mapeado, entao retorna Timeout`() {
        assertEquals(AppError.Timeout, mapThrowable(SocketTimeoutException()))
    }

    @Test
    fun `dado InterruptedIOException, quando mapeado, entao retorna Timeout`() {
        assertEquals(AppError.Timeout, mapThrowable(InterruptedIOException()))
    }

    @Test
    fun `dado SerializationException, quando mapeado, entao retorna Serialization`() {
        assertEquals(AppError.Serialization, mapThrowable(SerializationException("boom")))
    }

    @Test
    fun `dado HttpException 401, quando mapeado, entao retorna InvalidApiKey`() {
        assertEquals(AppError.InvalidApiKey, mapThrowable(httpException(401)))
    }

    @Test
    fun `dado HttpException 403, quando mapeado, entao retorna PlanNotAuthorized`() {
        assertEquals(AppError.PlanNotAuthorized, mapThrowable(httpException(403)))
    }

    @Test
    fun `dado HttpException 429 com Retry-After, quando mapeado, entao retorna RateLimited com o valor do header`() {
        val error = mapThrowable(httpException(429, mapOf("Retry-After" to "30")))
        assertEquals(AppError.RateLimited(30.seconds), error)
    }

    @Test
    fun `dado HttpException 429 sem Retry-After, quando mapeado, entao retorna RateLimited sem valor`() {
        val error = mapThrowable(httpException(429))
        assertEquals(AppError.RateLimited(null), error)
    }

    @Test
    fun `dado HttpException 500, quando mapeado, entao retorna Server`() {
        assertEquals(AppError.Server, mapThrowable(httpException(500)))
    }

    @Test
    fun `dado HttpException 503, quando mapeado, entao retorna Server`() {
        assertEquals(AppError.Server, mapThrowable(httpException(503)))
    }

    @Test
    fun `dado HttpException 404, quando mapeado, entao retorna Unknown`() {
        val error = mapThrowable(httpException(404))
        assert(error is AppError.Unknown)
    }

    @Test
    fun `dado Throwable generico, quando mapeado, entao retorna Unknown com a causa original`() {
        val cause = IllegalStateException("boom")
        val error = mapThrowable(cause)
        assertEquals(AppError.Unknown(cause), error)
    }

    @Test
    fun `dado CmcStatus com error_code 1001, quando mapeado, entao retorna InvalidApiKey`() {
        val status = CmcStatus(errorCode = 1001, errorMessage = "API key missing.")
        assertEquals(AppError.InvalidApiKey, mapCmcErrorCode(status))
    }

    @Test
    fun `dado CmcStatus com error_code 1002, quando mapeado, entao retorna InvalidApiKey`() {
        val status = CmcStatus(errorCode = 1002, errorMessage = "API key invalid.")
        assertEquals(AppError.InvalidApiKey, mapCmcErrorCode(status))
    }

    @Test
    fun `dado CmcStatus com error_code 1006, quando mapeado, entao retorna PlanNotAuthorized`() {
        val status = CmcStatus(errorCode = 1006, errorMessage = "Plan not authorized.")
        assertEquals(AppError.PlanNotAuthorized, mapCmcErrorCode(status))
    }

    @Test
    fun `dado CmcStatus com error_code 1008, quando mapeado, entao retorna RateLimited`() {
        val status = CmcStatus(errorCode = 1008, errorMessage = "Minute rate limit reached.")
        assertEquals(AppError.RateLimited(null), mapCmcErrorCode(status))
    }

    @Test
    fun `dado CmcStatus com error_code desconhecido, quando mapeado, entao retorna Unknown`() {
        val status = CmcStatus(errorCode = 9999, errorMessage = "Unmapped.")
        val error = mapCmcErrorCode(status)
        assert(error is AppError.Unknown)
    }

    @Test
    fun `dado Retry-After ausente, quando parseRetryAfter, entao retorna null`() {
        assertNull(parseRetryAfter(null))
    }

    @Test
    fun `dado Retry-After invalido, quando parseRetryAfter, entao retorna null`() {
        assertNull(parseRetryAfter("not-a-number"))
    }
}
