
package cl.duoc.nutriexpert.navigation

import cl.duoc.nutriexpert.viewmodel.crearDetalleJson

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import cl.duoc.nutriexpert.data.local.AppDatabase
import cl.duoc.nutriexpert.data.remote.planes.RetrofitPlanesClient
import cl.duoc.nutriexpert.data.repository.PlanRepository

import cl.duoc.nutriexpert.ui.plan.PlanCultivoScreen
import cl.duoc.nutriexpert.ui.plan.PlanFertilizantesScreen
import cl.duoc.nutriexpert.ui.plan.PlanResumenScreen
import cl.duoc.nutriexpert.ui.plan.PlanSemanaScreen
import cl.duoc.nutriexpert.ui.plan.MisPlanesScreen

import cl.duoc.nutriexpert.ui.views.LoginScreen
import cl.duoc.nutriexpert.ui.views.RegistroScreen
import cl.duoc.nutriexpert.ui.views.RecuperarScreen
import cl.duoc.nutriexpert.ui.views.InicioScreen
import cl.duoc.nutriexpert.ui.views.PerfilScreen
import cl.duoc.nutriexpert.ui.views.PrediosScreen
import cl.duoc.nutriexpert.ui.views.PredioFormScreen
import cl.duoc.nutriexpert.ui.views.SectoresScreen
import cl.duoc.nutriexpert.ui.views.CrearSectorScreen
import cl.duoc.nutriexpert.ui.views.AnalisisScreen
import cl.duoc.nutriexpert.ui.views.CrearAnalisisScreen

import cl.duoc.nutriexpert.data.local.SectorEntity
import cl.duoc.nutriexpert.viewmodel.SectoresViewModel
import cl.duoc.nutriexpert.viewmodel.AnalisisViewModel
import cl.duoc.nutriexpert.viewmodel.PlanViewModel
import cl.duoc.nutriexpert.viewmodel.PlanesViewModel
import cl.duoc.nutriexpert.seguridad.SesionManager

private const val DURACION_TRANSICION = 300

@Composable
fun AppNavigation(navController: NavHostController) {

    // ViewModel compartido por los cuatro pasos del asistente.
    val planViewModel: PlanViewModel = viewModel()

    // Sector seleccionado al comenzar un nuevo plan.
    var sectorPlanId by rememberSaveable {
        mutableIntStateOf(0)
    }

    // Repositorio de planes: Room + Retrofit.
    val context = LocalContext.current

    val planRepository = remember(context) {
        PlanRepository(
            planDao = AppDatabase.obtener(context).planDao(),
            api = RetrofitPlanesClient.api
        )
    }

    // Si ya hay una sesión guardada se entra directo a "Inicio";
    // si no, se parte en el login. Sin sesión no se llega a ninguna otra pantalla.
    val inicio = remember { if (SesionManager(context).haySesion()) "inicio" else "login" }

    NavHost(
        navController = navController,
        startDestination = inicio,
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

        // INICIO
        composable("inicio") {
            InicioScreen(navController)
        }

        // PERFIL
        composable("perfil") {
            PerfilScreen(navController)
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

            val predioId =
                backStackEntry.arguments?.getInt("predioId") ?: 0

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

                onVerPlanes = { sectorId ->
                    navController.navigate("mis_planes/$sectorId")
                },

                onCrearPlan = { sector ->

                    // Guardar el sector seleccionado.
                    sectorPlanId = sector.id

                    // Mantener los datos originales del asistente.
                    planViewModel.usarSector(
                        sector.cultivoId,
                        sector.hectareas
                    )

                    navController.navigate("plan/cultivo")
                },




                onVolver = {
                    if (!navController.popBackStack("predios", false)) {
                        navController.navigate("predios") {
                            launchSingleTop = true
                        }
                    }
                },
                onEditarSector = { sectorId ->
                    navController.navigate("editar_sector/$sectorId")
                }
            )

        }


        // MIS PLANES DEL SECTOR
        composable(
            route = "mis_planes/{sectorId}",
            arguments = listOf(
                navArgument("sectorId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val sectorId = backStackEntry.arguments?.getInt("sectorId") ?: 0

            val planesViewModel: PlanesViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(
                        modelClass: Class<T>
                    ): T {
                        return PlanesViewModel(planRepository) as T
                    }
                }
            )

            MisPlanesScreen(
                sectorId = sectorId,
                viewModel = planesViewModel,
                onVolver = { navController.popBackStack() }
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

            val predioId =
                backStackEntry.arguments?.getInt("predioId") ?: 0

            val sectoresViewModel: SectoresViewModel = viewModel()

            CrearSectorScreen(
                predioId = predioId,

                onGuardar = { sector ->
                    sectoresViewModel.guardarSector(sector) { ok ->
                        if (ok) {
                            navController.popBackStack()
                        }
                    }
                },

                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // EDITAR SECTOR: carga el sector y abre el mismo formulario con los datos puestos
        composable(
            route = "editar_sector/{sectorId}",
            arguments = listOf(
                navArgument("sectorId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val sectorId = backStackEntry.arguments?.getInt("sectorId") ?: 0

            val sectoresViewModel: SectoresViewModel = viewModel()

            // produceState: parte en null y se rellena cuando Room responde
            val sector by produceState<SectorEntity?>(initialValue = null, sectorId) {
                value = sectoresViewModel.obtenerSector(sectorId)
            }

            sector?.let { actual ->
                CrearSectorScreen(
                    predioId = actual.predioId,
                    sectorInicial = actual,
                    onGuardar = { editado ->
                        sectoresViewModel.guardarSector(editado) { ok ->
                            if (ok) navController.popBackStack()
                        }
                    },
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
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

            val sectorId =
                backStackEntry.arguments?.getInt("sectorId") ?: 0

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

            val sectorId =
                backStackEntry.arguments?.getInt("sectorId") ?: 0

            val analisisViewModel: AnalisisViewModel = viewModel()

            CrearAnalisisScreen(
                sectorId = sectorId,

                onGuardar = { analisis ->
                    analisisViewModel.guardarAnalisis(analisis) { ok ->
                        if (ok) {
                            navController.popBackStack()
                        }
                    }
                },

                onVolver = {
                    navController.popBackStack()
                }
            )
        }

        // ASISTENTE DEL PLAN NUTRICIONAL
        // Se conservan las cuatro pantallas originales.

        composable("plan/cultivo") {
            PlanCultivoScreen(navController, planViewModel)
        }

        composable("plan/fertilizantes") {
            PlanFertilizantesScreen(navController, planViewModel)
        }

        composable("plan/resumen") {
            PlanResumenScreen(navController, planViewModel)
        }

        composable("plan/semana") {
            PlanSemanaScreen(
                navController = navController,
                planViewModel = planViewModel,
                onGuardarPlan = { nombreTemporada, fechaInicio, fechaTermino ->

                    require(sectorPlanId > 0) {
                        "No se ha seleccionado un sector válido"
                    }

                    planRepository.guardarPlan(
                        sectorId = sectorPlanId,
                        nombreTemporada = nombreTemporada,
                        fechaInicio = fechaInicio,
                        fechaTermino = fechaTermino,
                        detalleJson = planViewModel.crearDetalleJson()
                    )
                }
            )
        }
    }
}


