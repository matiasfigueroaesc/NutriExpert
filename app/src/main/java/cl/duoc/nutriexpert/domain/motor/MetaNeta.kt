package cl.duoc.nutriexpert.domain.motor

import cl.duoc.nutriexpert.domain.model.Nutriente
import kotlin.math.max

/**
 * Meta nutricional neta y conversiones de análisis (regla provisoria del ERS v1.1,
 * se ajusta cuando llegue el Excel del cliente).
 */
object MetaNeta {

    /** Factores elemento → óxido. N se mantiene como N. */
    val FACTOR_OXIDO: Map<Nutriente, Double> = mapOf(
        Nutriente.N to 1.0,
        Nutriente.P2O5 to 2.291,
        Nutriente.K2O to 1.205,
        Nutriente.CaO to 1.399,
        Nutriente.MgO to 1.658
    )

    /** Masa de suelo en t/ha = prof(m) × 10.000 × DA × (1 − pedregosidad/100). */
    fun masaSueloTHa(profundidadCm: Double, densidadAparente: Double, pedregosidadPct: Double): Double =
        (profundidadCm / 100.0) * 10_000.0 * densidadAparente * (1.0 - pedregosidadPct / 100.0)

    /** kg/ha equivalentes a 1 ppm (30 cm y DA 1,3 → 3,9 kg/ha). */
    fun kgHaPorPpm(profundidadCm: Double, densidadAparente: Double, pedregosidadPct: Double): Double =
        masaSueloTHa(profundidadCm, densidadAparente, pedregosidadPct) / 1000.0

    /** Aporte del suelo en kg/ha de óxido a partir de ppm del elemento. */
    fun aporteSuelo(
        ppmElemento: Map<Nutriente, Double>,
        profundidadCm: Double,
        densidadAparente: Double,
        pedregosidadPct: Double
    ): Map<Nutriente, Double> {
        val factor = kgHaPorPpm(profundidadCm, densidadAparente, pedregosidadPct)
        return Nutriente.entries.associateWith { n ->
            (ppmElemento[n] ?: 0.0) * factor * FACTOR_OXIDO.getValue(n)
        }
    }

    /** Aporte del agua en kg/ha de óxido: mg/L × m³/ha / 1000. */
    fun aporteAgua(mgLElemento: Map<Nutriente, Double>, m3PorHa: Double): Map<Nutriente, Double> =
        Nutriente.entries.associateWith { n ->
            (mgLElemento[n] ?: 0.0) * m3PorHa / 1000.0 * FACTOR_OXIDO.getValue(n)
        }

    /**
     * Mₙ = max(0, Dₙ − fₙ·Sₙ − Wₙ) / (Eₙ/100).
     * @param fraccionSuelo fₙ, fracción aprovechable del suelo (0..1).
     * @param eficienciaPct Eₙ, eficiencia de aplicación en %.
     */
    fun calcular(
        demanda: Map<Nutriente, Double>,
        suelo: Map<Nutriente, Double> = emptyMap(),
        agua: Map<Nutriente, Double> = emptyMap(),
        fraccionSuelo: Map<Nutriente, Double> = emptyMap(),
        eficienciaPct: Map<Nutriente, Double> = emptyMap()
    ): Map<Nutriente, Double> = Nutriente.entries.associateWith { n ->
        val d = demanda[n] ?: 0.0
        val f = fraccionSuelo[n] ?: 0.0
        val s = suelo[n] ?: 0.0
        val w = agua[n] ?: 0.0
        val e = eficienciaPct[n] ?: 100.0
        max(0.0, d - f * s - w) / (e / 100.0)
    }
}
