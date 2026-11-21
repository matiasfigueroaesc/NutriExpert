package cl.duoc.nutriexpert.seguridad

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas del hash de contraseñas (PBKDF2 con sal)
class ContrasenasTest {

    @Test
    fun contrasenaCorrecta_seVerifica() {
        val guardado = Contrasenas.generarHash("agricola26")
        assertTrue(Contrasenas.verificar("agricola26", guardado))
    }

    @Test
    fun contrasenaIncorrecta_noSeVerifica() {
        val guardado = Contrasenas.generarHash("agricola26")
        assertFalse(Contrasenas.verificar("agricola27", guardado))
    }

    @Test
    fun elHashNoContieneLaContrasena() {
        val guardado = Contrasenas.generarHash("agricola26")
        assertFalse(guardado.contains("agricola26"))
    }

    @Test
    fun mismaContrasena_daHashesDistintos_porLaSal() {
        val uno = Contrasenas.generarHash("agricola26")
        val dos = Contrasenas.generarHash("agricola26")
        assertNotEquals(uno, dos)
        assertTrue(Contrasenas.verificar("agricola26", uno))
        assertTrue(Contrasenas.verificar("agricola26", dos))
    }

    @Test
    fun textoGuardadoDanado_noSeVerifica() {
        assertFalse(Contrasenas.verificar("agricola26", ""))
        assertFalse(Contrasenas.verificar("agricola26", "agricola26"))
        assertFalse(Contrasenas.verificar("agricola26", "pbkdf2:abc:zz:zz"))
        assertFalse(Contrasenas.verificar("agricola26", "otro:1000:00ff:00ff"))
    }
}
