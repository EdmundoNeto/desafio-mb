package br.com.edmundo.desafiomb.feature.exchanges.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import br.com.edmundo.desafiomb.core.domain.di.domainModule
import br.com.edmundo.desafiomb.core.domain.error.AppError
import br.com.edmundo.desafiomb.core.domain.model.PageLoad
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import br.com.edmundo.desafiomb.core.testing.di.fakeRepositoryModule
import br.com.edmundo.desafiomb.core.testing.fake.FakeExchangeRepository
import br.com.edmundo.desafiomb.core.testing.fixture.ExchangeFixtures
import br.com.edmundo.desafiomb.core.ui.testing.TestTags
import br.com.edmundo.desafiomb.feature.exchanges.di.exchangesModule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get

class ExchangeListScreenTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(domainModule, exchangesModule, fakeRepositoryModule)
    }

    @get:Rule
    val composeRule = createComposeRule()

    private val repository by lazy { get<ExchangeRepository>() as FakeExchangeRepository }

    @Test
    fun `UI01_dado_repositorio_fake_com_um_item_quando_tela_abre_entao_renderiza_logo_nome_volume_e_data`() {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = false))
        repository.emit(listOf(ExchangeFixtures.exchange()))

        composeRule.setContent { ExchangeListScreen(onExchangeClick = {}) }

        composeRule.onNodeWithTag(TestTags.exchangeListItem(270)).assertIsDisplayed()
    }

    @Test
    fun `UI02_dado_carregamento_em_andamento_quando_tela_abre_entao_mostra_skeleton`() {
        val gate = CompletableDeferred<Unit>()
        repository.loadPageGate = gate
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = false))

        composeRule.setContent { ExchangeListScreen(onExchangeClick = {}) }

        composeRule.onNodeWithTag(TestTags.LIST_SKELETON).assertIsDisplayed()
        gate.complete(Unit)
    }

    @Test
    fun `UI03_dado_falha_sem_cache_quando_tela_abre_entao_mostra_erro_e_retry_recarrega_a_lista`() {
        repository.loadPageResult = DomainResult.failure(AppError.NoConnection)

        composeRule.setContent { ExchangeListScreen(onExchangeClick = {}) }

        composeRule.onNodeWithTag(TestTags.FULL_SCREEN_ERROR).assertIsDisplayed()

        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = false))
        repository.emit(listOf(ExchangeFixtures.exchange()))
        composeRule.onNodeWithTag(TestTags.RETRY_BUTTON).performClick()

        composeRule.onNodeWithTag(TestTags.exchangeListItem(270)).assertIsDisplayed()
    }

    @Test
    fun `UI07_dado_hasMore_true_quando_rola_perto_do_fim_entao_dispara_append`() {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = true))
        repository.emit(ExchangeFixtures.exchangeList(20))

        composeRule.setContent { ExchangeListScreen(onExchangeClick = {}) }
        composeRule.onNodeWithTag(TestTags.exchangeListItem(1)).assertIsDisplayed()

        composeRule.onNodeWithTag(TestTags.EXCHANGE_LIST).performScrollToIndex(15)
        composeRule.waitForIdle()

        assert(repository.loadPageCalls.contains(1)) {
            "esperado onLoadMore acionado (loadPage(1)), chamadas obtidas: ${repository.loadPageCalls}"
        }
    }

    @Test
    fun `UI08_dado_onRefresh_acionado_pela_topbar_entao_repositorio_e_atualizado`() {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = false))
        repository.emit(listOf(ExchangeFixtures.exchange()))

        composeRule.setContent { ExchangeListScreen(onExchangeClick = {}) }
        composeRule.onNodeWithTag(TestTags.exchangeListItem(270)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Exchanges").performClick()
        composeRule.waitForIdle()

        assert(repository.refreshCalls == 1) { "esperado 1 chamada a refresh(), obtido ${repository.refreshCalls}" }
    }
}
