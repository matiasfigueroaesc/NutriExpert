package cl.duoc.nutriexpert.domain.motor

import cl.duoc.nutriexpert.domain.model.FertilizanteInfo
import cl.duoc.nutriexpert.domain.model.Nutriente

data class DosisFertilizante(
    val fertilizante: FertilizanteInfo,
    val nutrienteCubierto: Nutriente,
    val kgHa: Double
)

data class ResultadoDosis(
    val dosis: List<DosisFertilizante>,
    /** Pendiente final por nutriente: negativo = exceso, positivo = déficit. */
    val pendiente: Map<Nutriente, Double>,
    /** N aportado por fuentes que cubren otros nutrientes. */
    val nSecundario: Double
) {
    val totalKgHa: Double get() = dosis.sumOf { it.kgHa }

    fun excesos(): Map<Nutriente, Double> =
        pendiente.filterValues { it < -CalculoInverso.TOLERANCIA }.mapValues { -it.value }

    fun deficits(): Map<Nutriente, Double> =
        pendiente.filterValues { it > CalculoInverso.TOLERANCIA }

    /** Un nutriente con meta y sin fuente impide activar el plan. */
    val puedeActivarse: Boolean get() = deficits().isEmpty()
}

/**
 * Cálculo inverso de dosis (RF del bloque Plan nutricional, regla provisoria del ERS).
 *
 * Recorre P₂O₅ → CaO → K₂O → MgO → N. Para cada nutriente con meta pendiente elige,
 * entre las fuentes aún no usadas, la de mayor contenido de ese nutriente;
 * dosis = pendiente / (contenido/100) y descuenta su aporte a todos los nutrientes.
 */
object CalculoInverso {

    const val TOLERANCIA = 1e-6

    fun calcular(meta: Map<Nutriente, Double>, fuentes: List<FertilizanteInfo>): ResultadoDosis {
        val pendiente = Nutriente.entries.associateWith { meta[it] ?: 0.0 }.toMutableMap()
        val disponibles = fuentes.toMutableList()
        val dosis = mutableListOf<DosisFertilizante>()

        for (nutriente in Nutriente.ORDEN_CALCULO) {
            val falta = pendiente.getValue(nutriente)
            if (falta <= TOLERANCIA) continue
            val fuente = disponibles
                .filter { it.contenidoDe(nutriente) > 0.0 }
                .maxByOrNull { it.contenidoDe(nutriente) }
                ?: continue // queda como déficit
            val kgHa = falta / (fuente.contenidoDe(nutriente) / 100.0)
            dosis += DosisFertilizante(fuente, nutriente, kgHa)
            disponibles.remove(fuente)
            for (n in Nutriente.entries) {
                pendiente[n] = pendiente.getValue(n) - kgHa * fuente.contenidoDe(n) / 100.0
            }
        }

        val nSecundario = dosis
            .filter { it.nutrienteCubierto != Nutriente.N }
            .sumOf { it.kgHa * it.fertilizante.contenidoDe(Nutriente.N) / 100.0 }

        val limpio = pendiente.mapValues { (_, v) -> if (kotlin.math.abs(v) <= TOLERANCIA) 0.0 else v }
        return ResultadoDosis(dosis, limpio, nSecundario)
    }
}
