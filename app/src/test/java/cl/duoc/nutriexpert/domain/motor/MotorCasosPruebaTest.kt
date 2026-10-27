package cl.duoc.nutriexpert.domain.motor

import cl.duoc.nutriexpert.domain.model.CatalogoEjemplo
import cl.duoc.nutriexpert.domain.model.GrupoEstanque
import cl.duoc.nutriexpert.domain.model.Nutriente
import cl.duoc.nutriexpert.domain.model.mapaNutrientes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Casos de prueba del motor CP-01 a CP-03 (ERS v1.1). Tolerancia 0,01 kg. */
class MotorCasosPruebaTest {

    private val tol = 0.01
    private val c = CatalogoEjemplo
    private val fuentesCp01 = listOf(c.MAP, c.NITRATO_CALCIO, c.NITRATO_POTASIO, c.SULFATO_MAGNESIO, c.UREA)
    private val metaCp01 = mapaNutrientes(n = 200.0, p2o5 = 60.0, k2o = 250.0, cao = 100.0, mgo = 40.0)

    private fun dosisDe(r: ResultadoDosis, id: String) = r.dosis.first { it.fertilizante.id == id }.kgHa

    @Test
    fun cp01_calculoInverso() {
        val r = CalculoInverso.calcular(metaCp01, fuentesCp01)

        assertEquals(100.00, dosisDe(r, c.MAP.id), tol)
        assertEquals(370.37, dosisDe(r, c.NITRATO_CALCIO.id), tol)
        assertEquals(555.56, dosisDe(r, c.NITRATO_POTASIO.id), tol)
        assertEquals(250.00, dosisDe(r, c.SULFATO_MAGNESIO.id), tol)
        assertEquals(122.87, dosisDe(r, c.UREA.id), tol)
        assertEquals(1398.79, r.totalKgHa, tol)
        assertEquals(143.48, r.nSecundario, tol)
        assertEquals(
            listOf(Nutriente.P2O5, Nutriente.CaO, Nutriente.K2O, Nutriente.MgO, Nutriente.N),
            r.dosis.map { it.nutrienteCubierto }
        )
        assertTrue("Meta cubierta, sin excesos", r.excesos().isEmpty() && r.deficits().isEmpty())
    }

    @Test
    fun cp01_sinFuenteDeCalcioEsDeficit() {
        val r = CalculoInverso.calcular(metaCp01, fuentesCp01 - c.NITRATO_CALCIO)
        assertEquals(100.0, r.deficits().getValue(Nutriente.CaO), tol)
        assertFalse(r.puedeActivarse)
    }

    @Test
    fun cp02_kgPorRiego() {
        val dosis = 555.56
        val ha = 5.0
        val etapas = c.TOMATE.etapas
        val esperado = listOf(23.15, 46.30, 43.40, 52.08)
        etapas.forEachIndexed { i, e ->
            val kgEtapa = Distribucion.kgEtapa(dosis, e.porcentaje(Nutriente.K2O), ha)
            assertEquals(esperado[i], Distribucion.kgPorRiego(kgEtapa, e.semanas, e.riegosSemana), tol)
        }
        assertEquals(1388.89, Distribucion.kgEtapa(dosis, 50.0, ha), tol)
    }

    @Test
    fun cp02_reajusteProporcional() {
        val r = Distribucion.reajustar(listOf(15.0, 20.0, 50.0, 15.0), indice = 2, nuevo = 40.0)
        assertEquals(listOf(18.0, 24.0, 40.0, 18.0), r.map { Math.round(it * 100) / 100.0 })
        assertEquals(100.0, r.sum(), 1e-9)
    }

    @Test
    fun cp03_estanques() {
        val r = CalculoInverso.calcular(metaCp01, fuentesCp01)
        val etapa3 = c.TOMATE.etapas[2]
        val items = itemsSemanaEtapa(r.dosis, etapa3, superficieHa = 5.0)

        fun kg(nombre: String) = items.first { it.nombre == nombre }.kg
        assertEquals(81.02, kg(c.NITRATO_CALCIO.nombre), tol)
        assertEquals(15.63, kg(c.MAP.nombre), tol)
        assertEquals(54.69, kg(c.SULFATO_MAGNESIO.nombre), tol)
        assertEquals(173.61, kg(c.NITRATO_POTASIO.nombre), tol)
        assertEquals(26.88, kg(c.UREA.nombre), tol)

        val estanques = Estanques.armar(items, volumenL = 1000.0, cmaxPct = 20.0)
        val a = estanques.filter { it.grupo == GrupoEstanque.A }
        val b = estanques.filter { it.grupo == GrupoEstanque.B }

        assertEquals(1, a.size)
        assertEquals(107.90, a[0].kgTotal, tol)
        assertEquals(10.8, a[0].concentracionPct, 0.05)
        assertTrue(a[0].items.any { it.first == c.UREA.nombre })

        assertEquals(2, b.size)
        assertEquals(243.92, b.sumOf { it.kgTotal }, tol)
        b.forEach {
            assertEquals(121.96, it.kgTotal, tol)
            assertEquals(12.2, it.concentracionPct, 0.05)
            assertTrue(it.items.any { item -> item.first == c.NITRATO_POTASIO.nombre })
        }
    }

    @Test
    fun metaNeta_conversionesYRegla() {
        assertEquals(3.9, MetaNeta.kgHaPorPpm(30.0, 1.3, 0.0), 1e-9)
        val suelo = MetaNeta.aporteSuelo(mapaNutrientes(p2o5 = 10.0), 30.0, 1.3, 0.0)
        assertEquals(10 * 3.9 * 2.291, suelo.getValue(Nutriente.P2O5), 1e-9)

        val meta = MetaNeta.calcular(
            demanda = mapaNutrientes(n = 200.0, p2o5 = 60.0),
            suelo = mapaNutrientes(n = 100.0, p2o5 = 100.0),
            agua = mapaNutrientes(n = 20.0),
            fraccionSuelo = mapaNutrientes(n = 0.5, p2o5 = 1.0),
            eficienciaPct = mapaNutrientes(n = 80.0, p2o5 = 100.0)
        )
        assertEquals((200.0 - 50.0 - 20.0) / 0.8, meta.getValue(Nutriente.N), 1e-9)
        assertEquals(0.0, meta.getValue(Nutriente.P2O5), 1e-9)
    }
}
