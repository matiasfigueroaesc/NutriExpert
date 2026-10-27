package cl.duoc.nutriexpert.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.nutriexpert.domain.model.CatalogoEjemplo
import cl.duoc.nutriexpert.domain.model.CatalogoEjemplo.CultivoEjemplo
import cl.duoc.nutriexpert.domain.model.EtapaInfo
import cl.duoc.nutriexpert.domain.model.Nutriente
import cl.duoc.nutriexpert.domain.motor.CalculoInverso
import cl.duoc.nutriexpert.domain.motor.Distribucion
import cl.duoc.nutriexpert.domain.motor.Estanque
import cl.duoc.nutriexpert.domain.motor.Estanques
import cl.duoc.nutriexpert.domain.motor.MetaNeta
import cl.duoc.nutriexpert.domain.motor.ResultadoDosis
import cl.duoc.nutriexpert.domain.motor.itemsSemanaEtapa

// Estado del asistente: lo que el usuario va eligiendo en las 4 preguntas.
// Los valores por defecto reproducen los casos CP-01 a CP-03 (tomate, 5 ha, estanque de 1000 L).
data class PlanState(
    // Sección "Ajustes del asesor": una sola para todo el asistente, cerrada por defecto
    val mostrarAjustes: Boolean = false,
    // Paso 1 · Cultivo
    val cultivo: CultivoEjemplo = CatalogoEjemplo.TOMATE,
    val hectareas: String = "5",
    val demanda: Map<Nutriente, String> = textoDe(CatalogoEjemplo.TOMATE.demanda),
    val errorHectareas: String? = null,
    val erroresDemanda: Map<Nutriente, String> = emptyMap(),
    // Paso 2 · Fertilizantes
    val seleccionados: Set<String> = CatalogoEjemplo.PREFERIDOS_POR_DEFECTO,
    val errorFertilizantes: String? = null,
    // Paso 3 · Resumen
    val mostrarDetalle: Boolean = false,
    // Paso 4 · Semana: % de cada etapa por nutriente (ajuste del asesor) y estanques
    val nutrienteVista: Nutriente = Nutriente.K2O,
    val curvas: Map<Nutriente, List<Double>> = curvasDe(CatalogoEjemplo.TOMATE),
    val etapaEstanque: Int = 2,
    val volumenL: Double = 1000.0,
    val cmaxPct: Double = Estanques.CMAX_POR_DEFECTO
)

// Pasa la demanda del cultivo a texto para mostrarla en los campos (200.0 -> "200")
fun textoDe(valores: Map<Nutriente, Double>): Map<Nutriente, String> =
    valores.mapValues { (_, v) -> if (v % 1.0 == 0.0) v.toInt().toString() else v.toString() }

// Copia las curvas del cultivo: para cada nutriente, el % de cada etapa
fun curvasDe(cultivo: CultivoEjemplo): Map<Nutriente, List<Double>> =
    Nutriente.entries.associateWith { n -> cultivo.etapas.map { it.porcentaje(n) } }

// Acepta "7,5" o "7.5"; devuelve null si no es un número
fun aNumero(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

class PlanViewModel : ViewModel() {

    var estado by mutableStateOf(PlanState())
        private set

    // Catálogo de ejemplo; se cambia por el de Room cuando esté el de Fabián
    val cultivos = CatalogoEjemplo.CULTIVOS
    val fertilizantes = CatalogoEjemplo.FERTILIZANTES

    // Abre o cierra la sección "Ajustes del asesor"
    fun onMostrarAjustesChange(mostrar: Boolean) {
        estado = estado.copy(mostrarAjustes = mostrar)
    }

    // Superficie como número (0 si el texto no es válido)
    fun hectareasNumero(): Double = aNumero(estado.hectareas) ?: 0.0

    // ---------- Paso 1 · Cultivo ----------

    fun onCultivoChange(cultivo: CultivoEjemplo) {
        estado = estado.copy(
            cultivo = cultivo,
            demanda = textoDe(cultivo.demanda),
            erroresDemanda = emptyMap(),
            curvas = curvasDe(cultivo),
            etapaEstanque = estado.etapaEstanque.coerceAtMost(cultivo.etapas.size - 1)
        )
    }

    fun onHectareasChange(texto: String) {
        estado = estado.copy(hectareas = texto, errorHectareas = null)
    }

    fun onDemandaChange(nutriente: Nutriente, texto: String) {
        estado = estado.copy(
            demanda = estado.demanda + (nutriente to texto),
            erroresDemanda = estado.erroresDemanda - nutriente
        )
    }

    // Revisa el paso 1 antes de avanzar; deja un error por campo
    fun validarMeta(): Boolean {
        val ha = aNumero(estado.hectareas)
        val errorHa = if (ha == null || ha <= 0.0) "Ingresa una superficie mayor a 0" else null
        val errores = estado.demanda
            .filter { (_, texto) -> aNumero(texto).let { it == null || it < 0.0 } }
            .mapValues { "Ingresa un número de 0 o más" }
        estado = estado.copy(errorHectareas = errorHa, erroresDemanda = errores)
        return errorHa == null && errores.isEmpty()
    }

    // Meta neta en kg/ha. Por ahora sin análisis de suelo ni agua (llega con el formulario de Estefanía)
    fun meta(): Map<Nutriente, Double> =
        MetaNeta.calcular(demanda = estado.demanda.mapValues { aNumero(it.value) ?: 0.0 })

    // ---------- Paso 2 · Fertilizantes ----------

    fun onFertilizanteToggle(id: String) {
        val nuevos = if (id in estado.seleccionados) estado.seleccionados - id else estado.seleccionados + id
        estado = estado.copy(seleccionados = nuevos, errorFertilizantes = null)
    }

    fun validarFertilizantes(): Boolean {
        val error = if (estado.seleccionados.isEmpty()) "Elige al menos un fertilizante" else null
        estado = estado.copy(errorFertilizantes = error)
        return error == null
    }

    // ---------- Paso 3 · Resumen ----------

    fun onMostrarDetalleChange(mostrar: Boolean) {
        estado = estado.copy(mostrarDetalle = mostrar)
    }

    // Cálculo inverso con los fertilizantes marcados
    fun resultado(): ResultadoDosis =
        CalculoInverso.calcular(meta(), fertilizantes.filter { it.id in estado.seleccionados })

    // ---------- Paso 4 · Semana ----------

    fun onNutrienteVistaChange(nutriente: Nutriente) {
        estado = estado.copy(nutrienteVista = nutriente)
    }

    // Mueve el % de una etapa; las demás se reajustan para seguir sumando 100 %
    fun onPorcentajeChange(indiceEtapa: Int, nuevo: Double) {
        val n = estado.nutrienteVista
        val nueva = Distribucion.reajustar(estado.curvas.getValue(n), indiceEtapa, nuevo)
        estado = estado.copy(curvas = estado.curvas + (n to nueva))
    }

    // Etapas del cultivo con los % que dejó el usuario
    fun etapas(): List<EtapaInfo> =
        estado.cultivo.etapas.mapIndexed { i, etapa ->
            etapa.copy(curva = Nutriente.entries.associateWith { n -> estado.curvas.getValue(n)[i] })
        }

    fun onEtapaEstanqueChange(indice: Int) {
        estado = estado.copy(etapaEstanque = indice)
    }

    fun onVolumenChange(litros: Double) {
        estado = estado.copy(volumenL = litros)
    }

    fun onCmaxChange(porcentaje: Double) {
        estado = estado.copy(cmaxPct = porcentaje)
    }

    // Estanques A y B para una semana de la etapa elegida
    fun estanques(): List<Estanque> {
        val etapa = etapas()[estado.etapaEstanque]
        val items = itemsSemanaEtapa(resultado().dosis, etapa, hectareasNumero())
        return Estanques.armar(items, estado.volumenL, estado.cmaxPct)
    }
}
