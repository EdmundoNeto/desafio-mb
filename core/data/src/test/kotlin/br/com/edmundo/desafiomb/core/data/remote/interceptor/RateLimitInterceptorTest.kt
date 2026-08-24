package br.com.edmundo.desafiomb.core.data.remote.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RateLimitInterceptorTest {

    private lateinit var server: MockWebServer
    private var clockNanos = 0L
    private val sleptMillis = mutableListOf<Long>()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        clockNanos = 0L
        sleptMillis.clear()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun client(permitsPerMinute: Int) = OkHttpClient.Builder()
        .addInterceptor(
            RateLimitInterceptor(
                permitsPerMinute = permitsPerMinute,
                nanoTime = { clockNanos },
                sleep = { millis ->
                    sleptMillis += millis
                    clockNanos += millis * 1_000_000
                },
            ),
        )
        .build()

    private fun call(client: OkHttpClient) {
        server.enqueue(MockResponse().setResponseCode(200))
        client.newCall(Request.Builder().url(server.url("/v1/exchange/map")).build()).execute()
    }

    @Test
    fun `dado requisicoes dentro da capacidade, quando interceptadas, entao nao aciona espera`() {
        val client = client(permitsPerMinute = 2)

        call(client)
        call(client)

        assertTrue(sleptMillis.isEmpty())
    }

    @Test
    fun `dado requisicao alem da capacidade no mesmo minuto, quando interceptada, entao aciona espera pelo proximo token`() {
        val client = client(permitsPerMinute = 2)

        call(client)
        call(client)
        call(client)

        assertEquals(1, sleptMillis.size)
        assertTrue(sleptMillis.single() > 0)
    }

    @Test
    fun `dado tempo suficiente decorrido, quando interceptada, entao o refil libera nova requisicao sem espera`() {
        val client = client(permitsPerMinute = 2)

        call(client)
        call(client)
        clockNanos += 60_000_000_000L
        call(client)

        assertTrue(sleptMillis.isEmpty())
    }
}
