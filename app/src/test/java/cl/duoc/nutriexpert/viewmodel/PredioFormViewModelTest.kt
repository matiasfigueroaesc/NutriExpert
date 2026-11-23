package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import cl.duoc.nutriexpert.data.local.PredioEntity
import cl.duoc.nutriexpert.data.repository.PredioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Tests del formulario de predio: el repositorio es "de mentira" (MockK)
@OptIn(ExperimentalCoroutinesApi::class)
class PredioFormViewModelTest {

    private val repository = mockk<PredioRepository>(relaxed = true)
    private var sincronizacionesPedidas = 0

    @Before
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiar() = Dispatchers.resetMain()

    private fun crearVm() = PredioFormViewModel(
        mockk<Application>(relaxed = true),
        repository,
        pedirSincronizacion = { sincronizacionesPedidas++ }
    )

    @Test
    fun guardar_formularioVacio_muestraErroresYNoGuarda() {
        val vm = crearVm()

        vm.guardar()

        assertNotNull(vm.estado.errores.nombre)
        assertNotNull(vm.estado.errores.comuna)
        assertNotNull(vm.estado.errores.hectareas)
        assertFalse(vm.estado.guardado)
        coVerify(exactly = 0) { repository.guardar(any()) }
    }

    @Test
    fun guardar_datosCorrectos_guardaYPideSincronizar() {
        val vm = crearVm()
        vm.onNombreChange("  Los Aromos ")
        vm.onComunaChange("Quillota")
        vm.onHectareasChange("7,5")
        vm.onUbicacionChange(-32.88, -71.25)

        vm.guardar()

        coVerify {
            repository.guardar(match {
                it.nombre == "Los Aromos" && it.comuna == "Quillota" &&
                    it.hectareas == 7.5 && it.latitud == -32.88
            })
        }
        assertTrue(vm.estado.guardado)
        assertEquals(1, sincronizacionesPedidas)
    }

    @Test
    fun escribir_borraElErrorDeEseCampo() {
        val vm = crearVm()
        vm.guardar()

        vm.onNombreChange("Los Aromos")

        assertNull(vm.estado.errores.nombre)
        assertNotNull(vm.estado.errores.comuna)
    }

    @Test
    fun cargar_predioExistente_llenaElFormulario() {
        coEvery { repository.obtenerPorId(4) } returns
            PredioEntity(id = 4, nombre = "El Olivo", comuna = "Limache", hectareas = 3.0)
        val vm = crearVm()

        vm.cargar(4)

        assertEquals(4, vm.estado.id)
        assertEquals("El Olivo", vm.estado.nombre)
        assertEquals("3.0", vm.estado.hectareas)
    }

    @Test
    fun cargar_idCero_esPredioNuevo() {
        val vm = crearVm()

        vm.cargar(0)

        assertEquals("", vm.estado.nombre)
        coVerify(exactly = 0) { repository.obtenerPorId(any()) }
    }
}
