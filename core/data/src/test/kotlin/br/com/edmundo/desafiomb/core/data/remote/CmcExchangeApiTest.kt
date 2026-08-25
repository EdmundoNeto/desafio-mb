package br.com.edmundo.desafiomb.core.data.remote

import br.com.edmundo.desafiomb.core.testing.fixture.JsonFixtures
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

class CmcExchangeApiTest {
    private lateinit var server: MockWebServer
    private lateinit var api: CmcExchangeApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api =
            Retrofit
                .Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(cmcJson.asConverterFactory("application/json".toMediaType()))
                .build()
                .create()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `dado envelope valido, quando getExchangeMap, entao desserializa status e data`() =
        runTest {
            server.enqueue(MockResponse().setBody(JsonFixtures.exchangeMapResponse).setResponseCode(200))

            val response = api.getExchangeMap()

            assertEquals(0, response.status.errorCode)
            assertEquals(2, response.data?.size)
            assertEquals(270, response.data?.first()?.id)
        }

    @Test
    fun `dado campo desconhecido no JSON, quando getExchangeInfo, entao desserializa sem lancar excecao`() =
        runTest {
            server.enqueue(MockResponse().setBody(JsonFixtures.exchangeInfoResponse).setResponseCode(200))

            val response = api.getExchangeInfo(ids = "270")

            assertEquals("Binance", response.data?.getValue("270")?.name)
        }

    @Test
    fun `dado data nulo, quando getExchangeMap, entao data e null e o status carrega o error_code`() =
        runTest {
            server.enqueue(MockResponse().setBody(JsonFixtures.invalidApiKeyResponse).setResponseCode(200))

            val response = api.getExchangeMap()

            assertNull(response.data)
            assertEquals(1001, response.status.errorCode)
        }

    @Test
    fun `dado 200 com error_code diferente de zero, quando getExchangeAssets, entao o status reflete o erro sem lancar excecao de transporte`() =
        runTest {
            server.enqueue(MockResponse().setBody(JsonFixtures.invalidApiKeyResponse).setResponseCode(200))

            val response = api.getExchangeAssets(id = 270)

            assertTrue(response.status.errorCode != 0)
        }

    @Test
    fun `dado resposta de assets valida, quando getExchangeAssets, entao desserializa os campos aninhados`() =
        runTest {
            server.enqueue(MockResponse().setBody(JsonFixtures.exchangeAssetsResponse).setResponseCode(200))

            val response = api.getExchangeAssets(id = 270)

            val asset = response.data?.first()
            assertEquals("0xabc", asset?.walletAddress)
            assertEquals("ETH", asset?.currency?.symbol)
        }
}
