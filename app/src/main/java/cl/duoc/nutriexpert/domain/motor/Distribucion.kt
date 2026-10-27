package cl.duoc.nutriexpert.domain.motor

/** Distribución de una dosis por etapa fenológica (regla provisoria del ERS). */
object Distribucion {

    /** kg de la etapa para todo el sector = dosis (kg/ha) × % etapa × ha. */
    fun kgEtapa(dosisKgHa: Double, porcentajeEtapa: Double, superficieHa: Double): Double =
        dosisKgHa * porcentajeEtapa / 100.0 * superficieHa

    /** kg por riego = kg etapa / (semanas × riegos por semana). */
    fun kgPorRiego(kgEtapa: Double, semanas: Int, riegosSemana: Int): Double {
        val riegos = semanas * riegosSemana
        return if (riegos <= 0) 0.0 else kgEtapa / riegos
    }

    /** kg por semana de la etapa = kg etapa / semanas. */
    fun kgPorSemana(kgEtapa: Double, semanas: Int): Double =
        if (semanas <= 0) 0.0 else kgEtapa / semanas

    /**
     * Al fijar la etapa [indice] en [nuevo] %, las demás se reajustan en proporción:
     * p'ₖ = pₖ · (100 − x) / (100 − pₑ). Si las demás suman 0, se reparte en partes iguales.
     */
    fun reajustar(porcentajes: List<Double>, indice: Int, nuevo: Double): List<Double> {
        require(indice in porcentajes.indices) { "Etapa fuera de rango" }
        val x = nuevo.coerceIn(0.0, 100.0)
        if (porcentajes.size == 1) return listOf(100.0)
        val restoAnterior = 100.0 - porcentajes[indice]
        val restoNuevo = 100.0 - x
        return porcentajes.mapIndexed { k, p ->
            when {
                k == indice -> x
                restoAnterior <= 1e-9 -> restoNuevo / (porcentajes.size - 1)
                else -> p * restoNuevo / restoAnterior
            }
        }
    }
}
