package cl.duoc.nutriexpert.ui.plan

import cl.duoc.nutriexpert.data.local.PlanEntity
import org.json.JSONObject
import java.util.Locale

private fun numeroBonito(valor: Double): String = String.format(Locale("es", "CL"), "%.2f", valor)

fun textoDetallePlan(plan: PlanEntity): String {
    val lineas = mutableListOf<String>()
    lineas += "PLAN: ${plan.nombreTemporada}"
    lineas += "Inicio: ${plan.fechaInicio ?: "Sin fecha"}"
    lineas += "Termino: ${plan.fechaTermino ?: "Sin fecha"}"
    val detalle = plan.detalleJson
    if (detalle.isNullOrBlank()) {
        lineas += "Este plan fue guardado antes de incorporar el detalle nutricional."
        return lineas.joinToString("\n")
    }
    return try {
        val json = JSONObject(detalle)
        lineas += ""
        lineas += "CULTIVO Y SUPERFICIE"
        lineas += "Cultivo: ${json.optString("cultivo")}"
        lineas += "Hectareas: ${numeroBonito(json.optDouble("hectareas"))}"
        lineas += "Volumen estanque: ${numeroBonito(json.optDouble("volumenL"))} L"
        lineas += "Concentracion maxima: ${numeroBonito(json.optDouble("cmaxPct"))}%"
        val metas = json.optJSONObject("metasKgHa")
        if (metas != null) {
            lineas += ""
            lineas += "NECESIDADES NUTRICIONALES (kg/ha)"
            val claves = metas.keys()
            while (claves.hasNext()) {
                val clave = claves.next()
                lineas += "$clave: ${numeroBonito(metas.optDouble(clave))}"
            }
        }
        val dosis = json.optJSONArray("dosis")
        if (dosis != null) {
            lineas += ""
            lineas += "DOSIS DE FERTILIZANTES (kg/ha)"
            for (i in 0 until dosis.length()) {
                val item = dosis.optJSONObject(i) ?: continue
                lineas += "${item.optString("fertilizante")}: ${numeroBonito(item.optDouble("kgHa"))} kg/ha"
            }
        }
        val preparaciones = json.optJSONArray("preparacionesSemanales")
        if (preparaciones != null) {
            for (i in 0 until preparaciones.length()) {
                val etapa = preparaciones.optJSONObject(i) ?: continue
                lineas += ""
                lineas += "ETAPA: ${etapa.optString("etapa")}"
                val estanques = etapa.optJSONArray("estanques") ?: continue
                for (j in 0 until estanques.length()) {
                    val estanque = estanques.optJSONObject(j) ?: continue
                    lineas += "Estanque ${estanque.optString("nombre")}"
                    lineas += "Volumen: ${numeroBonito(estanque.optDouble("volumenL"))} L"
                    lineas += "Concentracion: ${numeroBonito(estanque.optDouble("concentracionPct"))}%"
                    if (estanque.optBoolean("sobreLimite")) lineas += "ADVERTENCIA: supera el limite de concentracion"
                    val items = estanque.optJSONArray("items") ?: continue
                    for (k in 0 until items.length()) {
                        val item = items.optJSONObject(k) ?: continue
                        lineas += "  ${item.optString("fertilizante")}: ${numeroBonito(item.optDouble("kgSemana"))} kg/semana"
                    }
                }
            }
        }
        lineas.joinToString("\n")
    } catch (e: Exception) {
        lineas.joinToString("\n") + "\nNo se pudo interpretar el detalle nutricional."
    }
}

