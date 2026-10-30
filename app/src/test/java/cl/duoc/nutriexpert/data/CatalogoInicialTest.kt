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

    // TODO: cuando el catálogo esté completo, agregar:
    // assertEquals(6, CatalogoInicial.fertilizantes.size)
    // assertEquals(2, CatalogoInicial.cultivos.size)
}
