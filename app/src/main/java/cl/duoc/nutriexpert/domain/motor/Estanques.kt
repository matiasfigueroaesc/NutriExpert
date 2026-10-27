package cl.duoc.nutriexpert.domain.motor

import cl.duoc.nutriexpert.domain.model.EtapaInfo
import cl.duoc.nutriexpert.domain.model.GrupoEstanque
import kotlin.math.ceil
import kotlin.math.max

data class ItemEstanque(
    val nombre: String,
    val grupo: GrupoEstanque,
    val kg: Double,
    val solubilidadGL: Double
)

data class Estanque(
    val nombre: String,
    val grupo: GrupoEstanque,
    val items: List<Pair<String, Double>>,
    val volumenL: Double,
    val cmaxPct: Double
) {
    val kgTotal: Double get() = items.sumOf { it.second }

    /** Concentración en % (kg por cada 100 L). */
    val concentracionPct: Double get() = if (volumenL <= 0) 0.0 else kgTotal / volumenL * 100.0

    val sobreLimite: Boolean get() = concentracionPct > cmaxPct + 1e-9
}

/**
 * Armado de estanques (regla provisoria del ERS).
 * - Grupo A (calcio) nunca con grupo B (sulfatos, fosfatos).
 * - Los "Ambos" se asignan de mayor a menor peso al grupo con menos kg.
 * - N° estanques = ceil(max(kg grupo/(V·Cmax/100), kg_f/(V·sol_f/1000))).
 */
object Estanques {

    const val CMAX_POR_DEFECTO = 20.0

    fun asignarGrupos(items: List<ItemEstanque>): Map<GrupoEstanque, List<ItemEstanque>> {
        val a = items.filter { it.grupo == GrupoEstanque.A }.toMutableList()
        val b = items.filter { it.grupo == GrupoEstanque.B }.toMutableList()
        items.filter { it.grupo == GrupoEstanque.AMBOS }
            .sortedByDescending { it.kg }
            .forEach { item ->
                if (a.sumOf { it.kg } < b.sumOf { it.kg }) a += item else b += item
            }
        return mapOf(GrupoEstanque.A to a, GrupoEstanque.B to b)
    }

    fun numeroEstanques(items: List<ItemEstanque>, volumenL: Double, cmaxPct: Double): Int {
        val total = items.sumOf { it.kg }
        if (total <= 0.0) return 0
        val porConcentracion = total / (volumenL * cmaxPct / 100.0)
        val porSolubilidad = items.maxOf { item ->
            if (item.solubilidadGL <= 0.0) 0.0 else item.kg / (volumenL * item.solubilidadGL / 1000.0)
        }
        return max(1, ceil(max(porConcentracion, porSolubilidad) - 1e-9).toInt())
    }

    fun armar(
        items: List<ItemEstanque>,
        volumenL: Double,
        cmaxPct: Double = CMAX_POR_DEFECTO
    ): List<Estanque> {
        val grupos = asignarGrupos(items.filter { it.kg > 0.0 })
        return listOf(GrupoEstanque.A, GrupoEstanque.B).flatMap { grupo ->
            val delGrupo = grupos.getValue(grupo).sortedByDescending { it.kg }
            val n = numeroEstanques(delGrupo, volumenL, cmaxPct)
            (1..n).map { i ->
                Estanque(
                    nombre = "${grupo.etiqueta}$i",
                    grupo = grupo,
                    items = delGrupo.map { it.nombre to it.kg / n },
                    volumenL = volumenL,
                    cmaxPct = cmaxPct
                )
            }
        }
    }
}

/**
 * Ítems de estanque para una semana de la [etapa]: cada fertilizante usa la curva
 * del nutriente que cubre. kg/semana = dosis × % etapa × ha / semanas.
 */
fun itemsSemanaEtapa(
    dosis: List<DosisFertilizante>,
    etapa: EtapaInfo,
    superficieHa: Double
): List<ItemEstanque> = dosis.map { d ->
    val kgEtapa = Distribucion.kgEtapa(d.kgHa, etapa.porcentaje(d.nutrienteCubierto), superficieHa)
    ItemEstanque(
        nombre = d.fertilizante.nombre,
        grupo = d.fertilizante.grupo,
        kg = Distribucion.kgPorSemana(kgEtapa, etapa.semanas),
        solubilidadGL = d.fertilizante.solubilidadGL
    )
}
