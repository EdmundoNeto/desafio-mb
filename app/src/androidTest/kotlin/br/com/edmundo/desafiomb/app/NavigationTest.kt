package br.com.edmundo.desafiomb.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.platform.app.InstrumentationRegistry
import br.com.edmundo.desafiomb.app.di.appModule
import br.com.edmundo.desafiomb.app.navigation.CmcNavHost
import br.com.edmundo.desafiomb.core.domain.di.domainModule
import br.com.edmundo.desafiomb.core.domain.model.PageLoad
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import br.com.edmundo.desafiomb.core.testing.di.fakeRepositoryModule
import br.com.edmundo.desafiomb.core.testing.fake.FakeExchangeRepository
import br.com.edmundo.desafiomb.core.testing.fixture.ExchangeFixtures
import br.com.edmundo.desafiomb.core.ui.testing.TestTags
import br.com.edmundo.desafiomb.feature.exchanges.di.exchangesModule
import br.com.edmundo.desafiomb.feature.exchanges.navigation.ExchangeDetailRoute
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get

class NavigationTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(appModule, domainModule, exchangesModule, fakeRepositoryModule)
    }

    @get:Rule
    val composeRule = createComposeRule()

    private val repository by lazy { get<ExchangeRepository>() as FakeExchangeRepository }

    @Test
    fun `UI04_dado_lista_carregada_quando_clico_num_item_entao_navega_para_o_detalhe_com_o_id_correto`() {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = false))
        repository.emit(listOf(ExchangeFixtures.exchange()))
        repository.emitDetail(ExchangeFixtures.exchangeDetail())

        lateinit var navController: TestNavHostController
        composeRule.setContent {
            navController = TestNavHostController(InstrumentationRegistry.getInstrumentation().targetContext)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            CmcNavHost(navController = navController)
        }

        composeRule.onNodeWithTag(TestTags.exchangeListItem(270)).performClick()
        composeRule.waitForIdle()

        val route = navController.currentBackStackEntry?.toRoute<ExchangeDetailRoute>()
        assertEquals(270, route?.exchangeId)
        composeRule.onNodeWithTag(TestTags.detailField("name")).assertIsDisplayed()
    }

    @Test
    fun `UI10_dado_scroll_na_lista_quando_navego_para_detalhe_e_volto_entao_a_posicao_de_scroll_e_preservada`() {
        repository.loadPageResult = DomainResult.success(PageLoad.Fresh(hasMore = false))
        repository.emit(ExchangeFixtures.exchangeList(30))
        repository.emitDetail(ExchangeFixtures.exchangeDetail())

        lateinit var navController: TestNavHostController
        composeRule.setContent {
            navController = TestNavHostController(InstrumentationRegistry.getInstrumentation().targetContext)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            CmcNavHost(navController = navController)
        }

        composeRule.onNodeWithTag(TestTags.EXCHANGE_LIST).performScrollToIndex(20)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.exchangeListItem(21)).performClick()
        composeRule.waitForIdle()
        composeRule.runOnUiThread { navController.popBackStack() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TestTags.exchangeListItem(21)).assertIsDisplayed()
    }
}
