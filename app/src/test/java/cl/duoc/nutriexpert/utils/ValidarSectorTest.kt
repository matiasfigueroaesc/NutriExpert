
package cl.duoc.nutriexpert.utils

import org.junit.Assert.*
import org.junit.Test

class ValidarSectorTest {

    @Test
    fun sectorValido_noTieneErrores() {
        val resultado = validarSector(
            nombre = "Sector Norte",
            cultivoId = "cultivo-tomate",
            hectareas = "5.5"
        )

        assertFalse(resultado.hayErrores())
    }

    @Test
    fun nombreVacio_muestraError() {
        val resultado = validarSector(
            nombre = "",
            cultivoId = "cultivo-tomate",
            hectareas = "5"
        )

        assertNotNull(resultado.nombre)
    }

    @Test
    fun nombreMuyCorto_muestraError() {
        val resultado = validarSector(
            nombre = "AB",
            cultivoId = "cultivo-tomate",
            hectareas = "5"
        )

        assertNotNull(resultado.nombre)
    }

    @Test
    fun cultivoVacio_muestraError() {
        val resultado = validarSector(
            nombre = "Sector Norte",
            cultivoId = "",
            hectareas = "5"
        )

        assertNotNull(resultado.cultivo)
    }

    @Test
    fun superficieNegativa_muestraError() {
        val resultado = validarSector(
            nombre = "Sector Norte",
            cultivoId = "cultivo-tomate",
            hectareas = "-3"
        )

        assertNotNull(resultado.hectareas)
    }

    @Test
    fun superficieCero_muestraError() {
        val resultado = validarSector(
            nombre = "Sector Norte",
            cultivoId = "cultivo-tomate",
            hectareas = "0"
        )

        assertNotNull(resultado.hectareas)
    }

    @Test
    fun superficieConComa_esValida() {
        val resultado = validarSector(
            nombre = "Sector Norte",
            cultivoId = "cultivo-lechuga",
            hectareas = "2,5"
        )

        assertFalse(resultado.hayErrores())
    }

    @Test
    fun superficieNoNumerica_muestraError() {
        val resultado = validarSector(
            nombre = "Sector Norte",
            cultivoId = "cultivo-tomate",
            hectareas = "abc"
        )

        assertNotNull(resultado.hectareas)
    }
}
