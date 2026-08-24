package br.com.edmundo.desafiomb.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.edmundo.desafiomb.feature.exchanges.list.ExchangeListScreen
import kotlinx.serialization.Serializable

@Serializable
data object ExchangeListRoute

@Serializable
data class ExchangeDetailRoute(val exchangeId: Int)

@Composable
fun CmcNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = ExchangeListRoute) {
        composable<ExchangeListRoute> {
            ExchangeListScreen(onExchangeClick = { id -> navController.navigate(ExchangeDetailRoute(id)) })
        }
        composable<ExchangeDetailRoute> {
            PlaceholderScreen(label = "Detalhe (E4)")
        }
    }
}
