package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import cl.duoc.nutriexpert.data.local.UsuarioEntity
import cl.duoc.nutriexpert.data.repository.AuthRepository
import cl.duoc.nutriexpert.data.repository.ResultadoAuth
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Tests de Mi perfil: el AuthRepository es de mentira (MockK), así no se usa Room ni el servidor.
@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelTest {

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val ana = UsuarioEntity(id = 42, nombre = "Ana", correo = "ana@correo.cl", hashContrasena = "x")

    @Before
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiar() = Dispatchers.resetMain()

    private fun crearVm(): PerfilViewModel {
        coEvery { authRepository.usuarioActual() } returns ana
        return PerfilViewModel(mockk<Application>(relaxed = true), authRepository)
    }

    @Test
    fun alAbrir_muestraNombreYCorreo() {
        val vm = crearVm()

        assertEquals("Ana", vm.estado.nombre)
        assertEquals("ana@correo.cl", vm.estado.correo)
        assertEquals("Ana", vm.estado.nombreEditado)
    }

    @Test
    fun guardarNombreVacio_muestraError_yNoLlamaAlServidor() {
        val vm = crearVm()

        vm.onNombreChange("  ")
        vm.guardarNombre()

        assertEquals("Ingresa tu nombre", vm.estado.errorNombre)
        coVerify(exactly = 0) { authRepository.actualizarNombre(any()) }
    }

    @Test
    fun guardarNombre_ok_actualizaLaPantalla() {
        coEvery { authRepository.actualizarNombre("Ana María") } returns
            ResultadoAuth.Exito(ana.copy(nombre = "Ana María"))
        val vm = crearVm()

        vm.onNombreChange("Ana María")
        vm.guardarNombre()

        assertEquals("Ana María", vm.estado.nombre)
        assertEquals("Nombre actualizado", vm.estado.mensaje)
        assertFalse(vm.estado.guardando)
    }

    @Test
    fun guardarNombre_sinRed_muestraElError_yMantieneElNombre() {
        coEvery { authRepository.actualizarNombre(any()) } returns
            ResultadoAuth.Error("Necesitas internet para cambiar tu nombre")
        val vm = crearVm()

        vm.onNombreChange("Otra")
        vm.guardarNombre()

        assertEquals("Necesitas internet para cambiar tu nombre", vm.estado.mensajeError)
        assertEquals("Ana", vm.estado.nombre)
    }

    @Test
    fun cerrarSesion_cierraYVuelveAlLogin() {
        val vm = crearVm()

        vm.cerrarSesion()

        verify { authRepository.cerrarSesion() }
        assertTrue(vm.estado.salir)
    }

    @Test
    fun borrarCuenta_ok_vuelveAlLogin() {
        coEvery { authRepository.eliminarCuenta() } returns null
        val vm = crearVm()

        vm.borrarCuenta()

        assertTrue(vm.estado.salir)
    }

    @Test
    fun borrarCuenta_sinRed_noSale_yMuestraElError() {
        coEvery { authRepository.eliminarCuenta() } returns "Necesitas internet para borrar tu cuenta"
        val vm = crearVm()

        vm.borrarCuenta()

        assertFalse(vm.estado.salir)
        assertEquals("Necesitas internet para borrar tu cuenta", vm.estado.mensajeError)
    }
}
