package br.com.edmundo.desafiomb.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

/** Rotas type-safe (RF-03). O detalhe recebe apenas o id, nunca o objeto (RF-01.4). */
@Serializable
data object ExchangeListRoute

@Serializable
data class ExchangeDetailRoute(val exchangeId: Int)

@Composable
fun CmcNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = ExchangeListRoute) {
        composable<ExchangeListRoute> {
            // E3: ExchangeListScreen(onExchangeClick = { navController.navigate(ExchangeDetailRoute(it)) })
            PlaceholderScreen(label = "Listagem (E3)")
        }
        composable<ExchangeDetailRoute> {
            // E4: ExchangeDetailScreen(onBack = navController::navigateUp)
            PlaceholderScreen(label = "Detalhe (E4)")
        }
    }
}
