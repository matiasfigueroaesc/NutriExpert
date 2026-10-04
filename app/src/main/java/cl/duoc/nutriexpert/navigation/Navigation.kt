package cl.duoc.nutriexpert.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.duoc.nutriexpert.ui.views.LoginScreen
import cl.duoc.nutriexpert.ui.views.PrediosScreen
import cl.duoc.nutriexpert.ui.views.RecuperarScreen
import cl.duoc.nutriexpert.ui.views.RegistroScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("registro") { RegistroScreen(navController) }
        composable("recuperar") { RecuperarScreen(navController) }
        composable("predios") { PrediosScreen(navController) }
    }
}
