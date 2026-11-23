package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import cl.duoc.nutriexpert.data.local.AnalisisEntity
import cl.duoc.nutriexpert.data.repository.AnalisisRepository
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
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalisisViewModelTest {

    private val repository = mockk<AnalisisRepository>(relaxed = true)
    private val analisis = AnalisisEntity(
        sectorId = 5, fecha = "2026-11-01",
        sueloN = 20.0, sueloP = 15.0, sueloK = 180.0, sueloCa = 1200.0, sueloMg = 150.0,
        aguaN = 1.0, aguaP = 0.5, aguaK = 2.0, aguaCa = 30.0, aguaMg = 10.0, aguaM3Ha = 5000.0
    )

    @Before
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiar() = Dispatchers.resetMain()

    private fun crearVm() = AnalisisViewModel(mockk<Application>(relaxed = true), repository)

    @Test
    fun cargarAnalisis_muestraLosDelSector() {
        every { repository.obtenerPorSector(5) } returns flowOf(listOf(analisis))
        val vm = crearVm()

        vm.cargarAnalisis(5)

        assertEquals(listOf(analisis), vm.analisis.value)
        assertNull(vm.error.value)
    }

    @Test
    fun cargarAnalisis_conError_muestraElMensaje() {
        every { repository.obtenerPorSector(5) } returns flow { throw IllegalStateException("falló Room") }
        val vm = crearVm()

        vm.cargarAnalisis(5)

        assertEquals("falló Room", vm.error.value)
    }

    @Test
    fun guardarAnalisis_avisaQueSalioBien() {
        val vm = crearVm()
        var resultado: Boolean? = null

        vm.guardarAnalisis(analisis) { resultado = it }

        coVerify { repository.guardar(analisis) }
        assertEquals(true, resultado)
        assertFalse(vm.guardando.value)
    }

    @Test
    fun guardarAnalisis_conError_avisaQueFallo() {
        coEvery { repository.guardar(any()) } throws IllegalStateException("disco lleno")
        val vm = crearVm()
        var resultado: Boolean? = null

        vm.guardarAnalisis(analisis) { resultado = it }

        assertEquals(false, resultado)
        assertEquals("disco lleno", vm.error.value)
    }

    @Test
    fun eliminarAnalisis_okYConError() {
        val vm = crearVm()
        var resultado: Boolean? = null

        vm.eliminarAnalisis(analisis) { resultado = it }
        assertEquals(true, resultado)

        coEvery { repository.eliminar(any()) } throws IllegalStateException("no se pudo")
        vm.eliminarAnalisis(analisis) { resultado = it }
        assertEquals(false, resultado)
        assertEquals("no se pudo", vm.error.value)
    }
}
