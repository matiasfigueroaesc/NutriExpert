package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import cl.duoc.nutriexpert.data.local.UsuarioEntity
import cl.duoc.nutriexpert.data.repository.AuthRepository
import cl.duoc.nutriexpert.data.repository.ResultadoAuth
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    // AuthRepository de mentira: así no se necesita Room ni el teléfono
    private val authRepository = mockk<AuthRepository>()

    @Before
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun limpiar() = Dispatchers.resetMain()

    private fun crearVm() = LoginViewModel(mockk<Application>(relaxed = true), authRepository)

    @Test
    fun ingresarConDatosCorrectos_iniciaSesion() {
        coEvery { authRepository.iniciarSesion("usuario@correo.cl", "Clave123") } returns
            ResultadoAuth.Exito(UsuarioEntity(id = 1, nombre = "Ana", correo = "usuario@correo.cl", hashContrasena = "x"))
        val vm = crearVm()

        vm.onCorreoChange("usuario@correo.cl")
        vm.onContrasenaChange("Clave123")
        vm.ingresar()

        assertTrue(vm.estado.ingresoCorrecto)
        assertEquals("", vm.estado.contrasena)   // no queda la contraseña en memoria
        assertFalse(vm.estado.cargando)
    }

    @Test
    fun ingresarConContrasenaIncorrecta_muestraElMensaje() {
        coEvery { authRepository.iniciarSesion(any(), any()) } returns
            ResultadoAuth.Error("Correo o contraseña incorrectos")
        val vm = crearVm()

        vm.onCorreoChange("usuario@correo.cl")
        vm.onContrasenaChange("Clave999")
        vm.ingresar()

        assertFalse(vm.estado.ingresoCorrecto)
        assertEquals("Correo o contraseña incorrectos", vm.estado.mensajeError)
    }

    @Test
    fun estadoInicialEstaVacio() {
        val vm = crearVm()

        assertEquals("", vm.estado.correo)
        assertEquals("", vm.estado.contrasena)
        assertEquals("", vm.estado.mensajeError)
        assertFalse(vm.estado.ingresoCorrecto)
    }

    @Test
    fun cambiarCorreoActualizaEstado() {
        val vm = crearVm()

        vm.onCorreoChange("usuario@correo.cl")

        assertEquals("usuario@correo.cl", vm.estado.correo)
    }

    @Test
    fun cambiarContrasenaActualizaEstado() {
        val vm = crearVm()

        vm.onContrasenaChange("Clave123")

        assertEquals("Clave123", vm.estado.contrasena)
    }

    @Test
    fun ingresarConCamposVaciosMuestraErrores() {
        val vm = crearVm()

        vm.ingresar()

        assertTrue(vm.estado.errores.hayErrores())
        assertFalse(vm.estado.ingresoCorrecto)
    }

    @Test
    fun ingresarConCorreoInvalidoMuestraError() {
        val vm = crearVm()

        vm.onCorreoChange("correo-invalido")
        vm.onContrasenaChange("Clave123")
        vm.ingresar()

        assertNotNull(vm.estado.errores.correo)
        assertFalse(vm.estado.ingresoCorrecto)
    }

    @Test
    fun cambiarCorreoLimpiaSuError() {
        val vm = crearVm()

        vm.ingresar()
        vm.onCorreoChange("nuevo@correo.cl")

        assertNull(vm.estado.errores.correo)
        assertEquals("", vm.estado.mensajeError)
    }

    @Test
    fun cambiarContrasenaLimpiaSuError() {
        val vm = crearVm()

        vm.ingresar()
        vm.onContrasenaChange("NuevaClave123")

        assertNull(vm.estado.errores.contrasena)
        assertEquals("", vm.estado.mensajeError)
    }

    @Test
    fun corregirCamposNoIniciaSesionAutomaticamente() {
        val vm = crearVm()

        vm.onCorreoChange("usuario@correo.cl")
        vm.onContrasenaChange("Clave123")

        assertFalse(vm.estado.ingresoCorrecto)
    }

    // ---------- Microservicio usuarios: sin red y bloqueo ----------

    @Test
    fun ingresarSinRed_primerIngreso_muestraQueFaltaInternet() {
        coEvery { authRepository.iniciarSesion(any(), any()) } returns
            ResultadoAuth.Error(AuthRepository.SIN_RED_PRIMER_INGRESO)
        val vm = crearVm()

        vm.onCorreoChange("usuario@correo.cl")
        vm.onContrasenaChange("Clave123")
        vm.ingresar()

        assertFalse(vm.estado.ingresoCorrecto)
        assertEquals(AuthRepository.SIN_RED_PRIMER_INGRESO, vm.estado.mensajeError)
        assertFalse(vm.estado.cargando)
    }

    @Test
    fun ingresarSinRed_conCuentaGuardada_entraIgual() {
        // Sin red el repositorio usa la copia local: para el ViewModel es un Exito normal
        coEvery { authRepository.iniciarSesion("usuario@correo.cl", "Clave123") } returns
            ResultadoAuth.Exito(UsuarioEntity(id = 42, nombre = "Ana", correo = "usuario@correo.cl", hashContrasena = "x"))
        val vm = crearVm()

        vm.onCorreoChange("usuario@correo.cl")
        vm.onContrasenaChange("Clave123")
        vm.ingresar()

        assertTrue(vm.estado.ingresoCorrecto)
    }

    @Test
    fun ingresarBloqueado_muestraCuantoEsperar() {
        coEvery { authRepository.iniciarSesion(any(), any()) } returns
            ResultadoAuth.Error("Demasiados intentos fallidos. Espera 30 segundos e inténtalo de nuevo.")
        val vm = crearVm()

        vm.onCorreoChange("usuario@correo.cl")
        vm.onContrasenaChange("Clave123")
        vm.ingresar()

        assertTrue(vm.estado.mensajeError.contains("30 segundos"))
        assertFalse(vm.estado.ingresoCorrecto)
    }
}
