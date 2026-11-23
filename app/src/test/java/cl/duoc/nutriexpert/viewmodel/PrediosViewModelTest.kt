package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import cl.duoc.nutriexpert.data.local.PredioEntity
import cl.duoc.nutriexpert.data.local.UsuarioEntity
import cl.duoc.nutriexpert.data.repository.AuthRepository
import cl.duoc.nutriexpert.data.repository.PredioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// Tests de PrediosViewModel: se le pasan repositorios de mentira y se revisa
// que la pantalla reciba la lista y que al borrar se pida sincronizar.
@OptIn(ExperimentalCoroutinesApi::class)
class PrediosViewModelTest {

    private val repository = mockk<PredioRepository>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private var sincronizacionesPedidas = 0
    private val predio = PredioEntity(id = 3, nombre = "Los Aromos", comuna = "Quillota", hectareas = 10.0)

    // viewModelScope usa el hilo principal de Android, que no existe en un test: se reemplaza
    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { repository.predios } returns flowOf(listOf(predio))
        coEvery { authRepository.usuarioActual() } returns
            UsuarioEntity(id = 1, nombre = "Mati", correo = "m@duoc.cl", hashContrasena = "x")
    }

    @After
    fun limpiar() = Dispatchers.resetMain()

    private fun crearViewModel() = PrediosViewModel(
        application = mockk<Application>(relaxed = true),
        repository = repository,
        authRepository = authRepository,
        pedirSincronizacion = { sincronizacionesPedidas++ }
    )

    @Test
    fun muestraLosPrediosYElNombre() = runTest {
        val viewModel = crearViewModel()

        assertEquals(listOf(predio), viewModel.predios.first { it.isNotEmpty() })
        assertEquals("Mati", viewModel.nombreUsuario)
    }

    @Test
    fun eliminar_borraYPideSincronizar() = runTest {
        val viewModel = crearViewModel()

        viewModel.eliminar(predio)

        coVerify { repository.eliminar(predio) }
        assertEquals(1, sincronizacionesPedidas)
    }

    @Test
    fun cerrarSesion_avisaAlAuthRepository() {
        val viewModel = crearViewModel()
        viewModel.cerrarSesion()
        verify { authRepository.cerrarSesion() }
    }
}
