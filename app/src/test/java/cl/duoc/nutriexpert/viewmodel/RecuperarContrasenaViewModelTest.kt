package cl.duoc.nutriexpert.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecuperarContrasenaViewModelTest {

    @Test
    fun correoVacio_muestraError() {
        val vm = RecuperarContrasenaViewModel()

        vm.solicitarRecuperacion()

        assertEquals("Ingresa tu correo electrónico", vm.estado.mensajeError)
        assertEquals("", vm.estado.mensajeInformativo)
    }

    @Test
    fun correoInvalido_muestraError() {
        val vm = RecuperarContrasenaViewModel()

        vm.onCorreoChange("sin-arroba")
        vm.solicitarRecuperacion()

        assertEquals("El correo no tiene un formato válido", vm.estado.mensajeError)
    }

    @Test
    fun correoValido_muestraMensajeInformativo() {
        val vm = RecuperarContrasenaViewModel()

        vm.onCorreoChange("ana@correo.cl")
        vm.solicitarRecuperacion()

        assertEquals("", vm.estado.mensajeError)
        assertTrue(vm.estado.mensajeInformativo.isNotEmpty())
    }

    @Test
    fun escribirOtroCorreo_limpiaLosMensajes() {
        val vm = RecuperarContrasenaViewModel()
        vm.solicitarRecuperacion()

        vm.onCorreoChange("a")

        assertEquals("", vm.estado.mensajeError)
        assertEquals("a", vm.estado.correo)
    }
}
