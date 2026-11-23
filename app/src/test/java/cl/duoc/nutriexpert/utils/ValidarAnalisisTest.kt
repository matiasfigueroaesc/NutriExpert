
package cl.duoc.nutriexpert.utils

import org.junit.Assert.*
import org.junit.Test

class ValidarAnalisisTest {

    private fun valoresValidos(): Map<String, String> = mapOf(
        "profundidadCm" to "30",
        "densidadAparente" to "1.3",
        "pedregosidadPct" to "10",
        "sueloN" to "20",
        "sueloP" to "15",
        "sueloK" to "100",
        "sueloCa" to "200",
        "sueloMg" to "50",
        "aguaN" to "5",
        "aguaP" to "3",
        "aguaK" to "10",
        "aguaCa" to "25",
        "aguaMg" to "8",
        "aguaM3Ha" to "100"
    )

    @Test
    fun analisisValido_noTieneErrores() {
        val resultado = validarAnalisis(
            fecha = "2026-10-28",
            valores = valoresValidos()
        )

        assertFalse(resultado.hayErrores())
    }

    @Test
    fun fechaVacia_muestraError() {
        val resultado = validarAnalisis(
            fecha = "",
            valores = valoresValidos()
        )

        assertNotNull(resultado.fecha)
    }

    @Test
    fun fechaConFormatoIncorrecto_muestraError() {
        val resultado = validarAnalisis(
            fecha = "28-10-2026",
            valores = valoresValidos()
        )

        assertNotNull(resultado.fecha)
    }

    @Test
    fun fechaInexistente_muestraError() {
        val resultado = validarAnalisis(
            fecha = "2026-02-30",
            valores = valoresValidos()
        )

        assertNotNull(resultado.fecha)
    }

    @Test
    fun profundidadCero_muestraError() {
        val valores = valoresValidos().toMutableMap()
        valores["profundidadCm"] = "0"

        val resultado = validarAnalisis("2026-10-28", valores)

        assertNotNull(resultado.campos["profundidadCm"])
    }

    @Test
    fun densidadNegativa_muestraError() {
        val valores = valoresValidos().toMutableMap()
        valores["densidadAparente"] = "-1.3"

        val resultado = validarAnalisis("2026-10-28", valores)

        assertNotNull(resultado.campos["densidadAparente"])
    }

    @Test
    fun pedregosidadMayorACien_muestraError() {
        val valores = valoresValidos().toMutableMap()
        valores["pedregosidadPct"] = "120"

        val resultado = validarAnalisis("2026-10-28", valores)

        assertNotNull(resultado.campos["pedregosidadPct"])
    }

    @Test
    fun nutrienteNegativo_muestraError() {
        val valores = valoresValidos().toMutableMap()
        valores["sueloN"] = "-5"

        val resultado = validarAnalisis("2026-10-28", valores)

        assertNotNull(resultado.campos["sueloN"])
    }

    @Test
    fun nutrienteVacio_muestraError() {
        val valores = valoresValidos().toMutableMap()
        valores["aguaP"] = ""

        val resultado = validarAnalisis("2026-10-28", valores)

        assertNotNull(resultado.campos["aguaP"])
    }

    @Test
    fun valorNoNumerico_muestraError() {
        val valores = valoresValidos().toMutableMap()
        valores["sueloK"] = "abc"

        val resultado = validarAnalisis("2026-10-28", valores)

        assertNotNull(resultado.campos["sueloK"])
    }

    @Test
    fun numerosConComa_sonValidos() {
        val valores = valoresValidos().toMutableMap()
        valores["densidadAparente"] = "1,3"
        valores["sueloN"] = "20,5"

        val resultado = validarAnalisis("2026-10-28", valores)

        assertFalse(resultado.hayErrores())
    }

    @Test
    fun aguaAplicadaNegativa_muestraError() {
        val valores = valoresValidos().toMutableMap()
        valores["aguaM3Ha"] = "-100"

        val resultado = validarAnalisis("2026-10-28", valores)

        assertNotNull(resultado.campos["aguaM3Ha"])
    }

    @Test
    fun campoObligatorioAusente_muestraError() {
        val valores = valoresValidos().toMutableMap()
        valores.remove("sueloMg")

        val resultado = validarAnalisis("2026-10-28", valores)

        assertNotNull(resultado.campos["sueloMg"])
    }

    @Test
    fun valoresEnCeroPermitidos_sonValidos() {
        val valores = valoresValidos().toMutableMap()
        valores["pedregosidadPct"] = "0"
        valores["sueloN"] = "0"
        valores["aguaP"] = "0"

        val resultado = validarAnalisis("2026-10-28", valores)

        assertFalse(resultado.hayErrores())
    }
}
