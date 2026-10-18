package cl.duoc.nutriexpert.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.duoc.nutriexpert.ui.views.LoginScreen
import cl.duoc.nutriexpert.ui.views.PredioFormScreen
import cl.duoc.nutriexpert.ui.views.PrediosScreen
import cl.duoc.nutriexpert.ui.views.RecuperarScreen
import cl.duoc.nutriexpert.ui.views.RegistroScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("registro") { RegistroScreen(navController) }
        composable("recuperar") { RecuperarScreen(navController) }
        composable("predios") { PrediosScreen(navController) }
        // "id" = 0 crea un predio nuevo; otro número edita ese predio
        composable(
            route = "predio_form/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            PredioFormScreen(navController, id)
        }
    }
}
