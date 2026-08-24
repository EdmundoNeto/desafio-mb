package br.com.edmundo.desafiomb.feature.exchanges.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.edmundo.desafiomb.core.domain.di.domainModule
import br.com.edmundo.desafiomb.core.domain.error.AppError
import br.com.edmundo.desafiomb.core.domain.repository.ExchangeRepository
import br.com.edmundo.desafiomb.core.domain.util.DomainResult
import br.com.edmundo.desafiomb.core.testing.di.fakeRepositoryModule
import br.com.edmundo.desafiomb.core.testing.fake.FakeExchangeRepository
import br.com.edmundo.desafiomb.core.testing.fixture.ExchangeFixtures
import br.com.edmundo.desafiomb.core.ui.testing.TestTags
import br.com.edmundo.desafiomb.feature.exchanges.di.exchangesModule
import br.com.edmundo.desafiomb.feature.exchanges.navigation.ExchangeDetailRoute
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get

class ExchangeDetailScreenTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(domainModule, exchangesModule, fakeRepositoryModule)
    }

    @get:Rule
    val composeRule = createComposeRule()

    private val repository by lazy { get<ExchangeRepository>() as FakeExchangeRepository }

    private fun setDetailContent(exchangeId: Int = 270) {
        composeRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = ExchangeDetailRoute(exchangeId)) {
                composable<ExchangeDetailRoute> {
                    ExchangeDetailScreen(onBack = {})
                }
            }
        }
    }

    @Test
    fun `UI05_dado_detalhe_e_assets_carregados_entao_renderiza_os_8_campos_e_a_lista_de_moedas`() {
        repository.emitDetail(ExchangeFixtures.exchangeDetail())
        repository.assetsResult = DomainResult.success(ExchangeFixtures.exchangeAssetList(2))

        setDetailContent()

        composeRule.onNodeWithTag(TestTags.detailField("logo")).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.detailField("name")).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.detailField("id")).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.detailField("description")).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.detailField("website")).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.detailField("maker_fee")).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.detailField("taker_fee")).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.detailField("date_launched")).assertIsDisplayed()
        composeRule.onNodeWithText("Coin 1").assertIsDisplayed()
    }

    @Test
    fun `UI06_dado_assets_com_falha_entao_header_renderiza_normalmente_e_apenas_a_secao_de_moedas_mostra_erro`() {
        repository.emitDetail(ExchangeFixtures.exchangeDetail())
        repository.assetsResult = DomainResult.failure(AppError.Server)

        setDetailContent()

        composeRule.onNodeWithTag(TestTags.detailField("name")).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.ASSETS_ERROR).assertIsDisplayed()
    }

    @Test
    fun `UI09_dado_lista_de_moedas_vazia_entao_mostra_mensagem_de_estado_vazio`() {
        repository.emitDetail(ExchangeFixtures.exchangeDetail())
        repository.assetsResult = DomainResult.success(emptyList())

        setDetailContent()

        composeRule.onNodeWithTag(TestTags.ASSETS_EMPTY).assertIsDisplayed()
    }
}
