
package cl.duoc.nutriexpert.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesUsuariosTest {

    // LOGIN

    @Test
    fun loginCorreoVacioMuestraError() {
        val errores = validarCamposLogin("", "12345678")

        assertEquals("Ingresa tu correo", errores.correo)
    }

    @Test
    fun loginCorreoInvalidoMuestraError() {
        val errores = validarCamposLogin("correo-invalido", "12345678")

        assertEquals(
            "El correo no tiene un formato válido",
            errores.correo
        )
    }

    @Test
    fun loginContrasenaCortaMuestraError() {
        val errores = validarCamposLogin("usuario@gmail.com", "123")

        assertEquals(
            "La contraseña debe tener al menos 8 caracteres",
            errores.contrasena
        )
    }

    @Test
    fun loginDatosValidosNoMuestranErrores() {
        val errores = validarCamposLogin(
            "usuario@gmail.com",
            "abcdefgh"
        )

        assertFalse(errores.hayErrores())
    }

    @Test
    fun loginValidoPermiteAccesoAlPrototipo() {
        val resultado = validarLogin(
            "usuario@gmail.com",
            "abcdefgh"
        )

        assertEquals("", resultado)
    }

    // REGISTRO

    @Test
    fun registroNombreVacioMuestraError() {
        val errores = validarCamposRegistro(
            nombre = "",
            correo = "usuario@gmail.com",
            contrasena = "abcdefgh",
            confirmarContrasena = "abcdefgh",
            aceptaPrivacidad = true
        )

        assertEquals("Ingresa tu nombre", errores.nombre)
    }

    @Test
    fun registroContrasenasDistintasMuestraError() {
        val errores = validarCamposRegistro(
            nombre = "Usuario",
            correo = "usuario@gmail.com",
            contrasena = "abcdefgh",
            confirmarContrasena = "12345678",
            aceptaPrivacidad = true
        )

        assertEquals(
            "Las contraseñas no coinciden",
            errores.confirmarContrasena
        )
    }

    @Test
    fun registroSinAceptarPrivacidadMuestraError() {
        val errores = validarCamposRegistro(
            nombre = "Usuario",
            correo = "usuario@gmail.com",
            contrasena = "abcdefgh",
            confirmarContrasena = "abcdefgh",
            aceptaPrivacidad = false
        )

        assertEquals(
            "Debes aceptar la política de privacidad",
            errores.privacidad
        )
    }

    @Test
    fun registroDatosValidosNoMuestranErrores() {
        val errores = validarCamposRegistro(
            nombre = "Usuario",
            correo = "usuario@gmail.com",
            contrasena = "abcdefgh",
            confirmarContrasena = "abcdefgh",
            aceptaPrivacidad = true
        )

        assertFalse(errores.hayErrores())
    }

    // RECUPERAR CONTRASEÑA

    @Test
    fun recuperacionCorreoVacioMuestraError() {
        val resultado = validarCorreoRecuperacion("")

        assertEquals(
            "Ingresa tu correo electrónico",
            resultado
        )
    }

    @Test
    fun recuperacionCorreoInvalidoMuestraError() {
        val resultado = validarCorreoRecuperacion("correo-invalido")

        assertEquals(
            "El correo no tiene un formato válido",
            resultado
        )
    }

    @Test
    fun recuperacionCorreoValidoNoMuestraError() {
        val resultado = validarCorreoRecuperacion(
            "usuario@gmail.com"
        )

        assertTrue(resultado.isEmpty())
    }
}
