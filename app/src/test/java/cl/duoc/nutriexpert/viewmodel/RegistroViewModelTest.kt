package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import cl.duoc.nutriexpert.data.local.UsuarioEntity
import cl.duoc.nutriexpert.data.repository.AuthRepository
import cl.duoc.nutriexpert.data.repository.ResultadoAuth
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Tests del registro: el AuthRepository es de mentira (MockK), así no se usa Room.
@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {

    private val authRepository = mockk<AuthRepository>()

    @Before
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiar() = Dispatchers.resetMain()

    private fun crearVm() = RegistroViewModel(mockk<Application>(relaxed = true), authRepository)

    // Llena el formulario con datos válidos
    private fun RegistroViewModel.llenar() {
        onNombreChange("Ana Pérez")
        onCorreoChange("ana@correo.cl")
        onContrasenaChange("Clave123")
        onConfirmarContrasenaChange("Clave123")
        onAceptaPrivacidadChange(true)
    }

    @Test
    fun registrarConDatosValidos_creaLaCuenta() {
        coEvery { authRepository.registrar("Ana Pérez", "ana@correo.cl", "Clave123") } returns
            ResultadoAuth.Exito(UsuarioEntity(id = 1, nombre = "Ana Pérez", correo = "ana@correo.cl", hashContrasena = "x"))
        val vm = crearVm()

        vm.llenar()
        vm.registrar()

        assertTrue(vm.estado.registroCorrecto)
        assertEquals("", vm.estado.contrasena)          // no queda la contraseña en memoria
        assertEquals("", vm.estado.confirmarContrasena)
        assertFalse(vm.estado.cargando)
    }

    @Test
    fun registrarConCorreoRepetido_muestraElErrorEnElCorreo() {
        coEvery { authRepository.registrar(any(), any(), any()) } returns
            ResultadoAuth.Error("Ya existe una cuenta con ese correo")
        val vm = crearVm()

        vm.llenar()
        vm.registrar()

        assertFalse(vm.estado.registroCorrecto)
        assertEquals("Ya existe una cuenta con ese correo", vm.estado.errores.correo)
    }

    @Test
    fun registrarConCamposVacios_noLlamaAlRepositorio() {
        val vm = crearVm()

        vm.registrar()

        assertTrue(vm.estado.errores.hayErrores())
        assertEquals("Ingresa tu nombre", vm.estado.errores.nombre)
        coVerify(exactly = 0) { authRepository.registrar(any(), any(), any()) }
    }

    @Test
    fun contrasenasDistintas_muestraError() {
        val vm = crearVm()

        vm.llenar()
        vm.onConfirmarContrasenaChange("Otra1234")
        vm.registrar()

        assertEquals("Las contraseñas no coinciden", vm.estado.errores.confirmarContrasena)
    }

    @Test
    fun sinAceptarPrivacidad_muestraError() {
        val vm = crearVm()

        vm.llenar()
        vm.onAceptaPrivacidadChange(false)
        vm.registrar()

        assertTrue(vm.estado.errores.privacidad.isNotEmpty())
    }

    @Test
    fun escribirEnUnCampo_limpiaSuError() {
        val vm = crearVm()
        vm.registrar()   // todos los campos quedan con error

        vm.onNombreChange("Ana")
        vm.onCorreoChange("ana@correo.cl")
        vm.onContrasenaChange("Clave123")
        vm.onConfirmarContrasenaChange("Clave123")
        vm.onAceptaPrivacidadChange(true)

        assertFalse(vm.estado.errores.hayErrores())
        assertEquals("", vm.estado.mensajeError)
    }
}
