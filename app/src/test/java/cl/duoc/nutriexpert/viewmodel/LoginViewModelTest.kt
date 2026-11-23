package cl.duoc.nutriexpert.viewmodel

import org.junit.Assert.*
import org.junit.Test

class LoginViewModelTest {

    @Test
    fun estadoInicialEstaVacio() {
        val vm = LoginViewModel()

        assertEquals("", vm.estado.correo)
        assertEquals("", vm.estado.contrasena)
        assertEquals("", vm.estado.mensajeError)
        assertFalse(vm.estado.ingresoCorrecto)
    }

    @Test
    fun cambiarCorreoActualizaEstado() {
        val vm = LoginViewModel()

        vm.onCorreoChange("usuario@correo.cl")

        assertEquals("usuario@correo.cl", vm.estado.correo)
    }

    @Test
    fun cambiarContrasenaActualizaEstado() {
        val vm = LoginViewModel()

        vm.onContrasenaChange("Clave123")

        assertEquals("Clave123", vm.estado.contrasena)
    }

    @Test
    fun ingresarConCamposVaciosMuestraErrores() {
        val vm = LoginViewModel()

        vm.ingresar()

        assertTrue(vm.estado.errores.hayErrores())
        assertFalse(vm.estado.ingresoCorrecto)
    }

    @Test
    fun ingresarConCorreoInvalidoMuestraError() {
        val vm = LoginViewModel()

        vm.onCorreoChange("correo-invalido")
        vm.onContrasenaChange("Clave123")
        vm.ingresar()

        assertNotNull(vm.estado.errores.correo)
        assertFalse(vm.estado.ingresoCorrecto)
    }

    @Test
    fun cambiarCorreoLimpiaSuError() {
        val vm = LoginViewModel()

        vm.ingresar()
        vm.onCorreoChange("nuevo@correo.cl")

        assertNull(vm.estado.errores.correo)
        assertEquals("", vm.estado.mensajeError)
    }

    @Test
    fun cambiarContrasenaLimpiaSuError() {
        val vm = LoginViewModel()

        vm.ingresar()
        vm.onContrasenaChange("NuevaClave123")

        assertNull(vm.estado.errores.contrasena)
        assertEquals("", vm.estado.mensajeError)
    }

    @Test
    fun corregirCamposNoIniciaSesionAutomaticamente() {
        val vm = LoginViewModel()

        vm.onCorreoChange("usuario@correo.cl")
        vm.onContrasenaChange("Clave123")

        assertFalse(vm.estado.ingresoCorrecto)
    }
}
