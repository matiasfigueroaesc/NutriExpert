package cl.duoc.nutriexpert.data

import cl.duoc.nutriexpert.data.local.CatalogoInicial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Revisa que los datos del catálogo sean coherentes antes de cargarlos en Room.
class CatalogoInicialTest {

    @Test
    fun idsDeFertilizantesNoSeRepiten() {
        val ids = CatalogoInicial.fertilizantes.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun idsDeCultivosNoSeRepiten() {
        val ids = CatalogoInicial.cultivos.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun cadaEtapaPerteneceAUnCultivoDelCatalogo() {
        val ids = CatalogoInicial.cultivos.map { it.id }.toSet()
        CatalogoInicial.etapas.forEach { etapa ->
            assertTrue("Etapa sin cultivo: ${etapa.cultivoId}", etapa.cultivoId in ids)
        }
    }

    @Test
    fun ordenDeEtapasNoSeRepiteEnUnCultivo() {
        CatalogoInicial.etapas.groupBy { it.cultivoId }.forEach { (_, etapas) ->
            val ordenes = etapas.map { it.orden }
            assertEquals(ordenes.size, ordenes.toSet().size)
        }
    }

    @Test
    fun gruposDeFertilizantesSonValidos() {
        CatalogoInicial.fertilizantes.forEach {
            assertTrue(it.grupo in setOf("A", "B", "AMBOS"))
        }
    }

    @Test
    fun catalogoTieneSeisFertilizantesYDosCultivos() {
        assertEquals(6, CatalogoInicial.fertilizantes.size)
        assertEquals(2, CatalogoInicial.cultivos.size)
    }

    @Test
    fun tomateTieneCuatroEtapasYLechugaTres() {
        val porCultivo = CatalogoInicial.etapas.groupBy { it.cultivoId }
        assertEquals(4, porCultivo["cultivo-tomate"]?.size)
        assertEquals(3, porCultivo["cultivo-lechuga"]?.size)
    }

    // La suma de cada nutriente en las etapas de un cultivo tiene que dar 100 %
    @Test
    fun porcentajesDeCadaCultivoSuman100() {
        CatalogoInicial.etapas.groupBy { it.cultivoId }.forEach { (cultivo, etapas) ->
            assertEquals("N de $cultivo", 100.0, etapas.sumOf { it.pctN }, 0.001)
            assertEquals("P2O5 de $cultivo", 100.0, etapas.sumOf { it.pctP2o5 }, 0.001)
            assertEquals("K2O de $cultivo", 100.0, etapas.sumOf { it.pctK2o }, 0.001)
            assertEquals("CaO de $cultivo", 100.0, etapas.sumOf { it.pctCao }, 0.001)
            assertEquals("MgO de $cultivo", 100.0, etapas.sumOf { it.pctMgo }, 0.001)
        }
    }
}
