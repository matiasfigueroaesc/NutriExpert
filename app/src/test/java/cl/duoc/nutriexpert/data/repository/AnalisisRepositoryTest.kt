package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.AnalisisDao
import cl.duoc.nutriexpert.data.local.AnalisisEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

// El repositorio de análisis solo pasa los pedidos al DAO: se revisa que lo haga bien
class AnalisisRepositoryTest {

    private val dao = mockk<AnalisisDao>(relaxed = true)
    private val repo = AnalisisRepository(dao)

    private val analisis = AnalisisEntity(
        id = 3, sectorId = 5, fecha = "2026-11-01",
        sueloN = 20.0, sueloP = 15.0, sueloK = 180.0, sueloCa = 1200.0, sueloMg = 150.0,
        aguaN = 1.0, aguaP = 0.5, aguaK = 2.0, aguaCa = 30.0, aguaMg = 10.0, aguaM3Ha = 5000.0
    )

    @Test
    fun obtenerPorSector_devuelveLosDelDao() = runTest {
        every { dao.obtenerPorSector(5) } returns flowOf(listOf(analisis))
        assertEquals(listOf(analisis), repo.obtenerPorSector(5).first())
    }

    @Test
    fun obtenerPorId_devuelveElDelDao() = runTest {
        coEvery { dao.obtenerPorId(3) } returns analisis
        assertEquals(analisis, repo.obtenerPorId(3))
    }

    @Test
    fun guardarYEliminar_llamanAlDao() = runTest {
        repo.guardar(analisis)
        repo.eliminar(analisis)
        coVerify { dao.guardar(analisis) }
        coVerify { dao.eliminar(analisis) }
    }
}
