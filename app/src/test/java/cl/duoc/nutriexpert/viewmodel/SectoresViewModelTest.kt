package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import cl.duoc.nutriexpert.data.local.SectorEntity
import cl.duoc.nutriexpert.data.repository.SectorRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SectoresViewModelTest {

    private val repository = mockk<SectorRepository>(relaxed = true)
    private var sincronizacionesPedidas = 0
    private val sector = SectorEntity(id = 5, predioId = 3, nombre = "Norte", cultivoId = "cultivo-tomate", hectareas = 4.0)

    @Before
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiar() = Dispatchers.resetMain()

    private fun crearViewModel() = SectoresViewModel(
        application = mockk<Application>(relaxed = true),
        repositorioInicial = repository,
        pedirSincronizacion = { sincronizacionesPedidas++ }
    )

    @Test
    fun cargarSectores_muestraLosDelPredio() {
        every { repository.obtenerPorPredio(3) } returns flowOf(listOf(sector))
        val viewModel = crearViewModel()

        viewModel.cargarSectores(3)

        assertEquals(listOf(sector), viewModel.sectores.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun cargarSectores_conError_muestraElMensaje() {
        every { repository.obtenerPorPredio(3) } returns flow { throw IllegalStateException("se cayó Room") }
        val viewModel = crearViewModel()

        viewModel.cargarSectores(3)

        assertEquals("se cayó Room", viewModel.error.value)
    }

    @Test
    fun guardarSector_guardaYPideSincronizar() {
        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.guardarSector(sector) { resultado = it }

        coVerify { repository.guardar(sector) }
        assertEquals(true, resultado)
        assertEquals(1, sincronizacionesPedidas)
        assertFalse(viewModel.guardando.value)
    }

    @Test
    fun guardarSector_conError_avisaQueFallo() {
        coEvery { repository.guardar(any()) } throws IllegalStateException("disco lleno")
        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.guardarSector(sector) { resultado = it }

        assertEquals(false, resultado)
        assertEquals("disco lleno", viewModel.error.value)
        assertEquals(0, sincronizacionesPedidas)
    }

    @Test
    fun eliminarSector_borraYPideSincronizar() {
        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.eliminarSector(sector) { resultado = it }

        coVerify { repository.eliminar(sector) }
        assertEquals(true, resultado)
        assertEquals(1, sincronizacionesPedidas)
    }

    @Test
    fun eliminarSector_conError_avisaQueFallo() {
        coEvery { repository.eliminar(any()) } throws IllegalStateException("no se pudo")
        val viewModel = crearViewModel()
        var resultado: Boolean? = null

        viewModel.eliminarSector(sector) { resultado = it }

        assertEquals(false, resultado)
        assertEquals("no se pudo", viewModel.error.value)
    }

    @Test
    fun obtenerSector_buscaEnElRepositorio() = runTest {
        coEvery { repository.obtenerPorId(5) } returns sector
        assertEquals(sector, crearViewModel().obtenerSector(5))
        assertTrue(sincronizacionesPedidas == 0)
    }
}
