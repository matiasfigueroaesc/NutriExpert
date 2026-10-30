
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
import cl.duoc.nutriexpert.ui.views.RegistroScreen
import cl.duoc.nutriexpert.ui.views.RecuperarScreen
import cl.duoc.nutriexpert.ui.views.PrediosScreen
import cl.duoc.nutriexpert.ui.views.PredioFormScreen
import cl.duoc.nutriexpert.ui.views.SectoresScreen
import cl.duoc.nutriexpert.ui.views.CrearSectorScreen
import cl.duoc.nutriexpert.ui.views.AnalisisScreen
import cl.duoc.nutriexpert.ui.views.CrearAnalisisScreen

import cl.duoc.nutriexpert.viewmodel.SectoresViewModel
import cl.duoc.nutriexpert.viewmodel.AnalisisViewModel
import cl.duoc.nutriexpert.viewmodel.PlanViewModel

private const val DURACION_TRANSICION = 300

@Composable
fun AppNavigation(navController: NavHostController) {
    // Un solo ViewModel para los 4 pasos del asistente, así no se pierde lo elegido
    val planViewModel: PlanViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login",
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

        // LOGIN
        composable("login") {
            LoginScreen(navController)
        }

        // REGISTRO
        composable("registro") {
            RegistroScreen(navController)
        }

        // RECUPERAR CONTRASEÑA
        composable("recuperar") {
            RecuperarScreen(navController)
        }

        // PREDIOS
        composable("predios") {
            PrediosScreen(navController)
        }

        // CREAR O EDITAR PREDIO
        composable(
            route = "predio_form/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val id = backStackEntry.arguments?.getInt("id") ?: 0

            PredioFormScreen(navController, id)
        }

        // SECTORES DEL PREDIO
        composable(
            route = "sectores/{predioId}",
            arguments = listOf(
                navArgument("predioId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val predioId = backStackEntry.arguments?.getInt("predioId") ?: 0

            val sectoresViewModel: SectoresViewModel = viewModel()

            SectoresScreen(
                predioId = predioId,
                viewModel = sectoresViewModel,
                onCrearSector = {
                    navController.navigate("crear_sector/$predioId")
                },
                onVerAnalisis = { sectorId ->
                    navController.navigate("analisis/$sectorId")
                },
                onCrearPlan = { sector ->
                    // El plan parte con el cultivo y las hectáreas del sector
                    planViewModel.usarSector(sector.cultivoId, sector.hectareas)
                    navController.navigate("plan/cultivo")
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // CREAR SECTOR
        composable(
            route = "crear_sector/{predioId}",
            arguments = listOf(
                navArgument("predioId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val predioId = backStackEntry.arguments?.getInt("predioId") ?: 0

            val sectoresViewModel: SectoresViewModel = viewModel()

            CrearSectorScreen(
                predioId = predioId,
                onGuardar = { sector ->
                    // Guarda en Room y, si salió bien, vuelve a la lista de sectores
                    sectoresViewModel.guardarSector(sector) { ok ->
                        if (ok) navController.popBackStack()
                    }
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // ANÁLISIS DEL SECTOR
        composable(
            route = "analisis/{sectorId}",
            arguments = listOf(
                navArgument("sectorId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val sectorId = backStackEntry.arguments?.getInt("sectorId") ?: 0

            val analisisViewModel: AnalisisViewModel = viewModel()

            AnalisisScreen(
                sectorId = sectorId,
                viewModel = analisisViewModel,
                onCrearAnalisis = {
                    navController.navigate("crear_analisis/$sectorId")
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // CREAR ANÁLISIS
        composable(
            route = "crear_analisis/{sectorId}",
            arguments = listOf(
                navArgument("sectorId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val sectorId = backStackEntry.arguments?.getInt("sectorId") ?: 0

            val analisisViewModel: AnalisisViewModel = viewModel()

            CrearAnalisisScreen(
                sectorId = sectorId,
                onGuardar = { analisis ->
                    // Guarda en Room y, si salió bien, vuelve a la lista de análisis
                    analisisViewModel.guardarAnalisis(analisis) { ok ->
                        if (ok) navController.popBackStack()
                    }
                },
                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // Asistente del plan nutricional en 4 preguntas
        composable("plan/cultivo") { PlanCultivoScreen(navController, planViewModel) }
        composable("plan/fertilizantes") { PlanFertilizantesScreen(navController, planViewModel) }
        composable("plan/resumen") { PlanResumenScreen(navController, planViewModel) }
        composable("plan/semana") { PlanSemanaScreen(navController, planViewModel) }
    }
}
