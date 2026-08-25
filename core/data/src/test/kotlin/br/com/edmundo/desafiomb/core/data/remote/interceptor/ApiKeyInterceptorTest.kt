package br.com.edmundo.desafiomb.core.data.remote.interceptor

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ApiKeyInterceptorTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `dado uma chamada, quando interceptada, entao adiciona o header X-CMC_PRO_API_KEY`() {
        server.enqueue(MockResponse().setResponseCode(200))
        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(ApiKeyInterceptor(apiKey = "secret-key"))
                .build()

        client.newCall(Request.Builder().url(server.url("/v1/exchange/map")).build()).execute()

        val recorded = server.takeRequest()
        assertEquals("secret-key", recorded.getHeader("X-CMC_PRO_API_KEY"))
    }

    @Test
    fun `dado uma chamada, quando interceptada, entao a key nunca aparece na query string`() {
        server.enqueue(MockResponse().setResponseCode(200))
        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(ApiKeyInterceptor(apiKey = "secret-key"))
                .build()

        client.newCall(Request.Builder().url(server.url("/v1/exchange/map")).build()).execute()

        val recorded = server.takeRequest()
        assertNull(recorded.requestUrl?.queryParameter("X-CMC_PRO_API_KEY"))
        assert(!recorded.path!!.contains("secret-key"))
    }
}
