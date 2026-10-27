package cl.duoc.nutriexpert.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.duoc.nutriexpert.ui.plan.PlanCultivoScreen
import cl.duoc.nutriexpert.ui.plan.PlanFertilizantesScreen
import cl.duoc.nutriexpert.ui.plan.PlanResumenScreen
import cl.duoc.nutriexpert.ui.plan.PlanSemanaScreen
import cl.duoc.nutriexpert.ui.views.LoginScreen
import cl.duoc.nutriexpert.ui.views.PredioFormScreen
import cl.duoc.nutriexpert.ui.views.PrediosScreen
import cl.duoc.nutriexpert.ui.views.RecuperarScreen
import cl.duoc.nutriexpert.ui.views.RegistroScreen
import cl.duoc.nutriexpert.viewmodel.PlanViewModel

// Duración de la animación al cambiar de pantalla (en milisegundos)
private const val DURACION_TRANSICION = 300

@Composable
fun AppNavigation(navController: NavHostController) {
    // Un solo ViewModel para los 4 pasos del asistente, así no se pierde lo elegido
    val planViewModel: PlanViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login",
        // Al avanzar, la pantalla nueva entra deslizándose desde la derecha
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(DURACION_TRANSICION)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(DURACION_TRANSICION)
            )
        },
        // Al volver, se desliza hacia la derecha
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(DURACION_TRANSICION)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(DURACION_TRANSICION)
            )
        }
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
        // Asistente del plan nutricional en 4 preguntas
        composable("plan/cultivo") { PlanCultivoScreen(navController, planViewModel) }
        composable("plan/fertilizantes") { PlanFertilizantesScreen(navController, planViewModel) }
        composable("plan/resumen") { PlanResumenScreen(navController, planViewModel) }
        composable("plan/semana") { PlanSemanaScreen(navController, planViewModel) }
    }
}
