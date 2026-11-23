package cl.duoc.nutriexpert.viewmodel

import cl.duoc.nutriexpert.data.local.PlanDao
import cl.duoc.nutriexpert.data.remote.planes.PlanApiService
import cl.duoc.nutriexpert.data.repository.PlanRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanesViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repository: PlanRepository
    private lateinit var viewModel: PlanesViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(dispatcher)
        repository = PlanRepository(
            mockk<PlanDao>(relaxed = true),
            mockk<PlanApiService>(relaxed = true)
        )
        viewModel = PlanesViewModel(repository)
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun estadoInicialEsCorrecto() {
        val estado = viewModel.uiState.value

        assertTrue(estado.planes.isEmpty())
        assertFalse(estado.cargando)
        assertFalse(estado.guardando)
        assertEquals(null, estado.error)
    }

    @Test
    fun guardarPlanConNombreVacioMuestraError() {
        viewModel.guardarPlan(1, "   ")

        assertEquals(
            "Ingresa un nombre para la temporada",
            viewModel.uiState.value.error
        )
        assertFalse(viewModel.uiState.value.guardando)
    }

    @Test
    fun editarPlanConNombreVacioMuestraError() {
        viewModel.editarPlan(1, "")

        assertTrue(viewModel.uiState.value.error?.isNotBlank() == true)
        assertFalse(viewModel.uiState.value.guardando)
    }

    @Test
    fun nombreVacioNoGuardaEnRepositorio() {
        viewModel.guardarPlan(1, "")

        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            "Ingresa un nombre para la temporada",
            viewModel.uiState.value.error
        )
    }

    @Test
    fun guardarPlanValidoFinalizaSinError() {
        viewModel.guardarPlan(3, "Verano", "2026-11-22", "2027-02-22")
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.guardando)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun editarPlanValidoFinalizaSinError() {
        viewModel.editarPlan(10, "Nueva temporada")
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.guardando)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun eliminarPlanFinalizaSinError() {
        viewModel.eliminarPlan(10)
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.guardando)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun sincronizarFinalizaSinError() {
        viewModel.sincronizar()
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.guardando)
        assertEquals(null, viewModel.uiState.value.error)
    }
}

