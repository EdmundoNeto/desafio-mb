package br.com.edmundo.desafiomb.core.data.repository

import br.com.edmundo.desafiomb.core.data.local.CmcDatabase
import br.com.edmundo.desafiomb.core.data.remote.dto.CmcResponse
import br.com.edmundo.desafiomb.core.data.remote.dto.CmcStatus
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeInfoDto
import br.com.edmundo.desafiomb.core.data.remote.dto.ExchangeMapItemDto
import br.com.edmundo.desafiomb.core.domain.model.PageLoad
import br.com.edmundo.desafiomb.core.testing.fake.FakeTimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExchangeRepositoryPagingTest {

    private lateinit var db: CmcDatabase
    private lateinit var api: FakeCmcExchangeApi
    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var repository: ExchangeRepositoryImpl

    private fun mapItems(count: Int) = (0 until count).map {
        ExchangeMapItemDto(id = it, name = "Exchange $it", slug = "exchange-$it")
    }

    private fun infoResponseFor(ids: List<Int>) = CmcResponse(
        status = CmcStatus(),
        data = ids.associate { id ->
            id.toString() to ExchangeInfoDto(id = id, name = "Exchange $id", spotVolumeUsd = id.toDouble())
        },
    )

    @Before
    fun setUp() {
        db = createInMemoryDatabase()
        api = FakeCmcExchangeApi()
        timeProvider = FakeTimeProvider(currentMillis = 1_000_000L)
        repository = ExchangeRepositoryImpl(
            api = api,
            db = db,
            indexDao = db.exchangeIndexDao(),
            exchangeDao = db.exchangeDao(),
            detailDao = db.exchangeDetailDao(),
            assetDao = db.exchangeAssetDao(),
            cacheMetaDao = db.cacheMetaDao(),
            timeProvider = timeProvider,
            ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `dado indice vazio, quando loadPage 0, entao busca map e info e retorna hasMore verdadeiro`() = runTest {
        val items = mapItems(60)
        api.onGetExchangeMap = { CmcResponse(CmcStatus(), items) }
        api.onGetExchangeInfo = { ids -> infoResponseFor(ids.split(",").map(String::toInt)) }

        val result = repository.loadPage(page = 0, pageSize = 25)

        val pageLoad = result.getOrNull()
        assertTrue(pageLoad is PageLoad.Fresh)
        assertTrue((pageLoad as PageLoad.Fresh).hasMore)
        assertEquals(1, api.mapCallCount)
        assertEquals(1, api.infoCallCount)
    }

    @Test
    fun `dado ultima pagina, quando loadPage, entao retorna hasMore falso`() = runTest {
        val items = mapItems(10)
        api.onGetExchangeMap = { CmcResponse(CmcStatus(), items) }
        api.onGetExchangeInfo = { ids -> infoResponseFor(ids.split(",").map(String::toInt)) }

        val result = repository.loadPage(page = 0, pageSize = 25)

        val pageLoad = result.getOrNull() as PageLoad.Fresh
        assertFalse(pageLoad.hasMore)
    }

    @Test
    fun `dado offset alem do indice, quando loadPage, entao retorna hasMore falso sem chamar info`() = runTest {
        val items = mapItems(10)
        api.onGetExchangeMap = { CmcResponse(CmcStatus(), items) }
        api.onGetExchangeInfo = { ids -> infoResponseFor(ids.split(",").map(String::toInt)) }

        val result = repository.loadPage(page = 5, pageSize = 25)

        val pageLoad = result.getOrNull() as PageLoad.Fresh
        assertFalse(pageLoad.hasMore)
        assertEquals(0, api.infoCallCount)
    }

    @Test
    fun `dado ids paginados, quando loadPage 0 e loadPage 1, entao os ids nao se repetem entre paginas`() = runTest {
        val items = mapItems(60)
        api.onGetExchangeMap = { CmcResponse(CmcStatus(), items) }
        api.onGetExchangeInfo = { ids -> infoResponseFor(ids.split(",").map(String::toInt)) }

        repository.loadPage(page = 0, pageSize = 25)
        repository.loadPage(page = 1, pageSize = 25)

        val page0Ids = db.exchangeIndexDao().idsForRange(0, 25).toSet()
        val page1Ids = db.exchangeIndexDao().idsForRange(25, 25).toSet()
        assertTrue(page0Ids.intersect(page1Ids).isEmpty())
    }

    @Test
    fun `dado a mesma pagina carregada duas vezes, quando loadPage, entao a segunda chamada nao gera nova requisicao de rede`() = runTest {
        val items = mapItems(60)
        api.onGetExchangeMap = { CmcResponse(CmcStatus(), items) }
        api.onGetExchangeInfo = { ids -> infoResponseFor(ids.split(",").map(String::toInt)) }

        repository.loadPage(page = 0, pageSize = 25)
        repository.loadPage(page = 0, pageSize = 25)

        assertEquals(1, api.mapCallCount)
        assertEquals(1, api.infoCallCount)
    }

    @Test
    fun `dado uma sessao que percorre 100 exchanges, quando roladas 4 paginas, entao consome apenas 1 chamada de map e 1 de info`() = runTest {
        val items = mapItems(100)
        api.onGetExchangeMap = { CmcResponse(CmcStatus(), items) }
        api.onGetExchangeInfo = { ids -> infoResponseFor(ids.split(",").map(String::toInt)) }

        repository.loadPage(page = 0, pageSize = 25)
        repository.loadPage(page = 1, pageSize = 25)
        repository.loadPage(page = 2, pageSize = 25)
        repository.loadPage(page = 3, pageSize = 25)

        assertEquals(1, api.mapCallCount)
        assertEquals(1, api.infoCallCount)
    }
}
