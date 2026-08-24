package br.com.edmundo.desafiomb.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.edmundo.desafiomb.feature.exchanges.detail.ExchangeDetailScreen
import br.com.edmundo.desafiomb.feature.exchanges.list.ExchangeListScreen
import br.com.edmundo.desafiomb.feature.exchanges.navigation.ExchangeDetailRoute
import br.com.edmundo.desafiomb.feature.exchanges.navigation.ExchangeListRoute

@Composable
fun CmcNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = ExchangeListRoute) {
        composable<ExchangeListRoute> {
            ExchangeListScreen(onExchangeClick = { id -> navController.navigate(ExchangeDetailRoute(id)) })
        }
        composable<ExchangeDetailRoute> {
            ExchangeDetailScreen(onBack = navController::navigateUp)
        }
    }
}
