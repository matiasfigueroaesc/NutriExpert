package cl.duoc.nutriexpert.domain.model

/**
 * Modelos del dominio. Kotlin puro: sin dependencias de Android ni de Room,
 * para que el motor se pruebe con JUnit en la JVM.
 */

/** Nutrientes del plan, siempre expresados en kg/ha de la forma óxido (salvo N). */
enum class Nutriente(val etiqueta: String) {
    N("N"),
    P2O5("P₂O₅"),
    K2O("K₂O"),
    CaO("CaO"),
    MgO("MgO");

    companion object {
        /** Orden del cálculo inverso definido en el ERS: P₂O₅ → CaO → K₂O → MgO → N. */
        val ORDEN_CALCULO = listOf(P2O5, CaO, K2O, MgO, N)
    }
}

/** Grupo de estanque: el calcio (A) nunca se mezcla con sulfatos ni fosfatos (B). */
enum class GrupoEstanque(val etiqueta: String) {
    A("A"),
    B("B"),
    AMBOS("Ambos")
}

/** Fertilizante tal como lo usa el motor. Contenidos en % (kg de nutriente por 100 kg). */
data class FertilizanteInfo(
    val id: String,
    val nombre: String,
    val contenido: Map<Nutriente, Double>,
    val solubilidadGL: Double,
    val grupo: GrupoEstanque
) {
    fun contenidoDe(n: Nutriente): Double = contenido[n] ?: 0.0
}

/** Etapa fenológica con su curva de absorción (% de la dosis total por nutriente). */
data class EtapaInfo(
    val orden: Int,
    val nombre: String,
    val semanas: Int,
    val riegosSemana: Int,
    val curva: Map<Nutriente, Double>
) {
    fun porcentaje(n: Nutriente): Double = curva[n] ?: 0.0
}

fun mapaNutrientes(
    n: Double = 0.0,
    p2o5: Double = 0.0,
    k2o: Double = 0.0,
    cao: Double = 0.0,
    mgo: Double = 0.0
): Map<Nutriente, Double> = mapOf(
    Nutriente.N to n,
    Nutriente.P2O5 to p2o5,
    Nutriente.K2O to k2o,
    Nutriente.CaO to cao,
    Nutriente.MgO to mgo
)
