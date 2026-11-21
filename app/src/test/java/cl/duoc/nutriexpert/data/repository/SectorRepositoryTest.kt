package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.PredioDao
import cl.duoc.nutriexpert.data.local.SectorDao
import cl.duoc.nutriexpert.data.local.SectorEntity
import cl.duoc.nutriexpert.data.remote.SectorApiService
import cl.duoc.nutriexpert.data.remote.SectorDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class SectorRepositoryTest {

    private val dao = mockk<SectorDao>(relaxed = true)
    private val predioDao = mockk<PredioDao>(relaxed = true)
    private val api = mockk<SectorApiService>()
    private val repository = SectorRepository(dao, predioDao, api)

    private val sector = SectorEntity(id = 5, predioId = 3, nombre = "Norte", cultivoId = "cultivo-tomate", hectareas = 4.0)
    private fun dto(id: Long) = SectorDto(id = id, predioId = 50, nombre = "Norte", cultivo = "cultivo-tomate", superficieHa = 4.0)
    // Respuesta de error fingida (400, 404, 500...)
    private fun <T> error(codigo: Int): Response<T> = mockk {
        every { isSuccessful } returns false
        every { code() } returns codigo
        every { body() } returns null
    }

    @Test
    fun leer_preguntaARoom() = runTest {
        every { dao.obtenerPorPredio(3) } returns flowOf(listOf(sector))
        coEvery { dao.obtenerPorId(5) } returns sector

        assertEquals(listOf(sector), repository.obtenerPorPredio(3).first())
        assertEquals(sector, repository.obtenerPorId(5))
    }

    @Test
    fun guardar_quedaPendienteYMantieneElRemoteId() = runTest {
        coEvery { dao.obtenerPorId(5) } returns sector.copy(remoteId = 70, sincronizado = true)
        val guardado = slot<SectorEntity>()
        coEvery { dao.guardar(capture(guardado)) } returns 5L

        repository.guardar(sector.copy(nombre = "Sur"))

        assertEquals(70L, guardado.captured.remoteId)
        assertFalse(guardado.captured.sincronizado)
    }

    @Test
    fun guardarNuevo_noBuscaElAnterior() = runTest {
        repository.guardar(sector.copy(id = 0))
        coVerify(exactly = 0) { dao.obtenerPorId(any()) }
        coVerify { dao.guardar(any()) }
    }

    @Test
    fun eliminar_loMarcaComoEliminado() = runTest {
        repository.eliminar(sector)
        coVerify { dao.marcarEliminado(5) }
    }

    @Test
    fun sincronizar_sectorNuevo_usaElIdDelPredioEnElServidor() = runTest {
        coEvery { dao.obtenerPendientes() } returns listOf(sector)
        coEvery { predioDao.obtenerRemoteId(3) } returns 50
        val enviado = slot<SectorDto>()
        coEvery { api.crear(capture(enviado)) } returns Response.success(dto(70))

        assertTrue(repository.sincronizarPendientes())
        assertEquals(50L, enviado.captured.predioId)
        coVerify { dao.marcarSincronizado(5, 70) }
    }

    @Test
    fun sincronizar_siElPredioNoEstaEnElServidor_esperaSuTurno() = runTest {
        coEvery { dao.obtenerPendientes() } returns listOf(sector)
        coEvery { predioDao.obtenerRemoteId(3) } returns null

        assertFalse(repository.sincronizarPendientes())
        coVerify(exactly = 0) { api.crear(any()) }
    }

    @Test
    fun sincronizar_sectorEditado_loActualiza() = runTest {
        coEvery { dao.obtenerPendientes() } returns listOf(sector.copy(remoteId = 70))
        coEvery { predioDao.obtenerRemoteId(3) } returns 50
        coEvery { api.actualizar(70, any()) } returns Response.success(dto(70))

        assertTrue(repository.sincronizarPendientes())
        coVerify { dao.marcarSincronizado(5, 70) }
    }

    @Test
    fun sincronizar_conErrores_quedaPendiente() = runTest {
        coEvery { dao.obtenerPendientes() } returns listOf(sector, sector.copy(id = 6, remoteId = 71))
        coEvery { predioDao.obtenerRemoteId(3) } returns 50
        coEvery { api.crear(any()) } returns error(400)
        coEvery { api.actualizar(71, any()) } throws IOException("sin red")

        assertFalse(repository.sincronizarPendientes())
        coVerify(exactly = 0) { dao.marcarSincronizado(any(), any()) }
    }

    @Test
    fun sincronizar_sectorBorrado() = runTest {
        coEvery { dao.obtenerPendientes() } returns listOf(
            sector.copy(remoteId = 70, eliminado = true),
            sector.copy(id = 6, eliminado = true),
            sector.copy(id = 7, remoteId = 72, eliminado = true)
        )
        coEvery { api.eliminar(70) } returns Response.success(Unit)
        coEvery { api.eliminar(72) } returns error(500)   // falla: este queda pendiente

        assertFalse(repository.sincronizarPendientes())
        coVerify { dao.borrarDefinitivo(5) }
        coVerify { dao.borrarDefinitivo(6) }
        coVerify(exactly = 0) { dao.borrarDefinitivo(7) }
    }

    @Test
    fun descargar_agregaLosQueFaltan() = runTest {
        coEvery { predioDao.obtenerRemoteId(3) } returns 50
        coEvery { api.listar(50) } returns Response.success(listOf(dto(70), dto(71), dto(72), SectorDto(null, 50, "x", "y", 1.0)))
        coEvery { dao.obtenerPorRemoteId(70) } returns null
        coEvery { dao.obtenerPorRemoteId(71) } returns sector.copy(id = 9, remoteId = 71, sincronizado = true)
        coEvery { dao.obtenerPorRemoteId(72) } returns sector.copy(id = 10, remoteId = 72, sincronizado = false)
        val guardados = mutableListOf<SectorEntity>()
        coEvery { dao.guardar(capture(guardados)) } returns 1L

        assertTrue(repository.descargar(3))

        assertEquals(2, guardados.size)
        assertEquals(3, guardados[0].predioId)   // se guarda con el id LOCAL del predio
        assertEquals(9, guardados[1].id)
    }

    @Test
    fun descargar_casosQueNoSePuede() = runTest {
        coEvery { predioDao.obtenerRemoteId(3) } returns null
        assertFalse(repository.descargar(3))            // el predio no está en el servidor

        coEvery { predioDao.obtenerRemoteId(3) } returns 50
        coEvery { api.listar(50) } returns error(500)
        assertFalse(repository.descargar(3))

        coEvery { api.listar(50) } throws IOException("sin red")
        assertFalse(repository.descargar(3))
    }
}
