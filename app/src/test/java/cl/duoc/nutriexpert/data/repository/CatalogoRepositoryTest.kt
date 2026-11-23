package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.CultivoDao
import cl.duoc.nutriexpert.data.local.CultivoEntity
import cl.duoc.nutriexpert.data.local.EtapaDao
import cl.duoc.nutriexpert.data.local.EtapaEntity
import cl.duoc.nutriexpert.data.local.FertilizanteDao
import cl.duoc.nutriexpert.data.local.FertilizanteEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

// El catálogo es solo lectura: se revisa que entregue lo que tiene Room
class CatalogoRepositoryTest {

    private val cultivoDao = mockk<CultivoDao>()
    private val fertilizanteDao = mockk<FertilizanteDao>()
    private val etapaDao = mockk<EtapaDao>()
    private val repo = CatalogoRepository(cultivoDao, fertilizanteDao, etapaDao)

    private val tomate = CultivoEntity("cultivo-tomate", "Tomate", 200.0, 80.0, 300.0, 150.0, 40.0)
    private val nitrato = FertilizanteEntity("fert-nitrato-calcio", "Nitrato de calcio", 15.5, 0.0, 0.0, 26.0, 0.0, 1200.0, "B")
    private val etapa = EtapaEntity(1, "cultivo-tomate", 1, "Establecimiento", 4, 3, 10.0, 15.0, 8.0, 10.0, 10.0)

    @Test
    fun cultivos_devuelveLaListaDeRoom() = runTest {
        every { cultivoDao.obtenerTodos() } returns flowOf(listOf(tomate))
        assertEquals(listOf(tomate), repo.cultivos().first())
    }

    @Test
    fun cultivo_buscaPorId() = runTest {
        coEvery { cultivoDao.obtenerPorId("cultivo-tomate") } returns tomate
        assertEquals(tomate, repo.cultivo("cultivo-tomate"))
    }

    @Test
    fun fertilizantes_devuelveLaListaDeRoom() = runTest {
        every { fertilizanteDao.obtenerTodos() } returns flowOf(listOf(nitrato))
        assertEquals(listOf(nitrato), repo.fertilizantes().first())
    }

    @Test
    fun etapas_devuelveLasDelCultivo() = runTest {
        every { etapaDao.obtenerDeCultivo("cultivo-tomate") } returns flowOf(listOf(etapa))
        assertEquals(listOf(etapa), repo.etapas("cultivo-tomate").first())
    }
}
