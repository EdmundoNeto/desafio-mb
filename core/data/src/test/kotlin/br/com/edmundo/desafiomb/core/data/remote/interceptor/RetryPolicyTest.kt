package br.com.edmundo.desafiomb.core.data.remote.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RetryPolicyTest {

    private lateinit var server: MockWebServer
    private val sleptMillis = mutableListOf<Long>()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        sleptMillis.clear()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun client(maxAttempts: Int = 3) = OkHttpClient.Builder()
        .addInterceptor(
            RetryInterceptor(
                maxAttempts = maxAttempts,
                sleep = { millis -> sleptMillis += millis },
            ),
        )
        .build()

    private fun execute(client: OkHttpClient) =
        client.newCall(Request.Builder().url(server.url("/v1/exchange/map")).build()).execute()

    @Test
    fun `dado 503 seguido de 200, quando interceptado, entao tenta novamente e retorna sucesso`() {
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(200))

        val response = execute(client())

        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `dado erro transitorio persistente, quando interceptado, entao esgota as tentativas e retorna a ultima resposta`() {
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(MockResponse().setResponseCode(503))

        val response = execute(client(maxAttempts = 3))

        assertEquals(503, response.code)
        assertEquals(3, server.requestCount)
    }

    @Test
    fun `dado 401, quando interceptado, entao nao tenta novamente`() {
        server.enqueue(MockResponse().setResponseCode(401))

        val response = execute(client())

        assertEquals(401, response.code)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `dado 403, quando interceptado, entao nao tenta novamente`() {
        server.enqueue(MockResponse().setResponseCode(403))

        val response = execute(client())

        assertEquals(403, response.code)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `dado 404, quando interceptado, entao nao tenta novamente`() {
        server.enqueue(MockResponse().setResponseCode(404))

        val response = execute(client())

        assertEquals(404, response.code)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `dado IOException seguido de 200, quando interceptado, entao tenta novamente e retorna sucesso`() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        server.enqueue(MockResponse().setResponseCode(200))

        val response = execute(client())

        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `dado 429 com Retry-After, quando interceptado, entao respeita o tempo do header`() {
        server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "2"))
        server.enqueue(MockResponse().setResponseCode(200))

        execute(client())

        assertEquals(2000L, sleptMillis.first())
    }

    @Test
    fun `dado 5xx sem Retry-After, quando interceptado, entao aplica backoff exponencial limitado a 8s`() {
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(200))

        execute(client(maxAttempts = 3))

        assertEquals(2, sleptMillis.size)
        sleptMillis.forEach { millis -> assert(millis in 1..8000) }
    }
}
