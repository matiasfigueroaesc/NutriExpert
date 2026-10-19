package cl.duoc.nutriexpert.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas de la validación del formulario de predio (RF-09)
class ValidarPredioTest {

    @Test
    fun datosCorrectos_noHayErrores() {
        val errores = validarPredio("Los Aromos", "Quillota", "7,5")
        assertFalse(errores.hayErrores())
    }

    @Test
    fun camposVacios_cadaCampoTieneSuError() {
        val errores = validarPredio("", " ", "")
        assertEquals("Ingresa el nombre del predio", errores.nombre)
        assertEquals("Ingresa la comuna", errores.comuna)
        assertEquals("Ingresa la superficie", errores.hectareas)
    }

    @Test
    fun superficieConLetras_esInvalida() {
        val errores = validarPredio("Los Aromos", "Quillota", "siete")
        assertEquals("Usa solo números, ej: 7,5", errores.hectareas)
        assertNull(errores.nombre)
    }

    @Test
    fun superficieCeroONegativa_esInvalida() {
        assertTrue(validarPredio("Los Aromos", "Quillota", "0").hectareas != null)
        assertTrue(validarPredio("Los Aromos", "Quillota", "-2").hectareas != null)
    }

    @Test
    fun aceptaPuntoYComaDecimal() {
        assertNull(validarPredio("Los Aromos", "Quillota", "7.5").hectareas)
        assertNull(validarPredio("Los Aromos", "Quillota", "7,5").hectareas)
    }
}
