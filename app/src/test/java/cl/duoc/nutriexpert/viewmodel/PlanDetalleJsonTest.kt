package cl.duoc.nutriexpert.viewmodel

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// El detalle del plan se guarda como texto JSON: se revisa que tenga las partes principales
class PlanDetalleJsonTest {

    @Test
    fun crearDetalleJson_tieneCultivoHectareasDosisYEtapas() {
        val vm = PlanViewModel()
        vm.onHectareasChange("5")

        val json = JSONObject(vm.crearDetalleJson())

        assertEquals(vm.estado.cultivo.id, json.getString("cultivo"))
        assertEquals(5.0, json.getDouble("hectareas"), 0.001)
        assertTrue(json.has("metasKgHa"))
        assertTrue(json.getJSONArray("dosis").length() > 0)
        assertTrue(json.getJSONArray("etapas").length() > 0)
        assertEquals(
            json.getJSONArray("etapas").length(),
            json.getJSONArray("preparacionesSemanales").length()
        )
    }
}
