package cl.duoc.nutriexpert.viewmodel

import org.json.JSONArray
import org.json.JSONObject
import cl.duoc.nutriexpert.domain.motor.Estanques
import cl.duoc.nutriexpert.domain.motor.itemsSemanaEtapa
import cl.duoc.nutriexpert.domain.model.Nutriente

fun PlanViewModel.crearDetalleJson(): String {
    val json = JSONObject()
    val resultado = resultado()
    json.put("cultivo", estado.cultivo.id)
    json.put("hectareas", hectareasNumero())
    json.put("volumenL", estado.volumenL)
    json.put("cmaxPct", estado.cmaxPct)
    val metas = JSONObject()
    meta().forEach { (nutriente, cantidad) -> metas.put(nutriente.name, cantidad) }
    json.put("metasKgHa", metas)
    val dosis = JSONArray()
    resultado.dosis.forEach { item ->
        dosis.put(JSONObject().put("fertilizante", item.fertilizante.nombre).put("nutriente", item.nutrienteCubierto.name).put("kgHa", item.kgHa))
    }
    json.put("dosis", dosis)
    val etapasJson = JSONArray()
    etapas().forEach { etapa ->
        val curvas = JSONObject()
        Nutriente.entries.forEach { nutriente -> curvas.put(nutriente.name, etapa.porcentaje(nutriente)) }
        etapasJson.put(JSONObject().put("nombre", etapa.nombre).put("semanas", etapa.semanas).put("riegosSemana", etapa.riegosSemana).put("curvas", curvas))
    }
    json.put("etapas", etapasJson)
    val preparaciones = JSONArray()
    etapas().forEach { etapa ->
        val estanquesEtapa = Estanques.armar(
            itemsSemanaEtapa(resultado.dosis, etapa, hectareasNumero()),
            estado.volumenL,
            estado.cmaxPct
        )
        val estanquesJson = JSONArray()
        estanquesEtapa.forEach { estanque ->
            val itemsJson = JSONArray()
            estanque.items.forEach { (nombre, kg) ->
                itemsJson.put(JSONObject().put("fertilizante", nombre).put("kgSemana", kg))
            }
            estanquesJson.put(JSONObject().put("nombre", estanque.nombre).put("volumenL", estanque.volumenL).put("concentracionPct", estanque.concentracionPct).put("sobreLimite", estanque.sobreLimite).put("items", itemsJson))
        }
        preparaciones.put(JSONObject().put("etapa", etapa.nombre).put("estanques", estanquesJson))
    }
    json.put("preparacionesSemanales", preparaciones)
    return json.toString()
}

