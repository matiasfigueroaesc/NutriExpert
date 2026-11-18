package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.PredioDao
import cl.duoc.nutriexpert.data.local.PredioEntity
import cl.duoc.nutriexpert.data.local.SectorDao
import cl.duoc.nutriexpert.data.remote.PredioApiService
import cl.duoc.nutriexpert.data.remote.PredioDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

// Tests del repositorio de predios. MockK "finge" el DAO (Room) y el ApiService (servidor),
// así se puede probar qué hace el repositorio con y sin red, sin teléfono ni servidor real.
class PredioRepositoryTest {

    private val dao = mockk<PredioDao>(relaxed = true)
    private val sectorDao = mockk<SectorDao>(relaxed = true)
    private val api = mockk<PredioApiService>()
    private lateinit var repository: PredioRepository

    private val predio = PredioEntity(id = 3, usuarioId = 1, nombre = "Los Aromos", comuna = "Quillota", hectareas = 10.0)
    private fun dto(id: Long) = PredioDto(id = id, usuarioId = 1, nombre = "Los Aromos", comuna = "Quillota", superficieHa = 10.0)
    // Respuesta de error fingida (400, 404, 500...)
    private fun <T> error(codigo: Int): Response<T> = mockk {
        every { isSuccessful } returns false
        every { code() } returns codigo
        every { body() } returns null
    }

    @Before
    fun preparar() {
        every { dao.obtenerPorUsuario(1) } returns flowOf(listOf(predio))
        repository = PredioRepository(dao, sectorDao, api, usuarioId = 1)
    }

    @Test
    fun guardarNuevo_quedaEnRoomComoPendiente() = runTest {
        val guardado = slot<PredioEntity>()
        coEvery { dao.guardar(capture(guardado)) } returns 1L

        repository.guardar(predio.copy(id = 0, usuarioId = 99))

        assertEquals(1, guardado.captured.usuarioId)          // el dueño lo pone el repositorio
        assertFalse(guardado.captured.sincronizado)
        coVerify(exactly = 0) { api.crear(any()) }            // guardar NO usa internet
    }

    @Test
    fun guardarEditado_mantieneElRemoteId() = runTest {
        coEvery { dao.obtenerPorId(3, 1) } returns predio.copy(remoteId = 50, sincronizado = true)
        val guardado = slot<PredioEntity>()
        coEvery { dao.guardar(capture(guardado)) } returns 3L

        repository.guardar(predio.copy(nombre = "Nuevo nombre"))

        assertEquals(50L, guardado.captured.remoteId)
        assertFalse(guardado.captured.sincronizado)
    }

    @Test
    fun eliminar_escondeElPredioYBorraSusSectores() = runTest {
        repository.eliminar(predio)

        coVerify { dao.marcarEliminado(3, 1) }
        coVerify { sectorDao.borrarPorPredio(3) }
    }

    @Test
    fun obtenerPorId_preguntaARoom() = runTest {
        coEvery { dao.obtenerPorId(3, 1) } returns predio
        assertEquals(predio, repository.obtenerPorId(3))
    }

    @Test
    fun sincronizar_predioNuevo_loCreaYGuardaElRemoteId() = runTest {
        coEvery { dao.obtenerPendientes(1) } returns listOf(predio)
        coEvery { api.crear(any()) } returns Response.success(dto(50))

        assertTrue(repository.sincronizarPendientes())
        coVerify { dao.marcarSincronizado(3, 50) }
    }

    @Test
    fun sincronizar_sinRed_quedaPendiente() = runTest {
        coEvery { dao.obtenerPendientes(1) } returns listOf(predio)
        coEvery { api.crear(any()) } throws IOException("sin red")

        assertFalse(repository.sincronizarPendientes())
        coVerify(exactly = 0) { dao.marcarSincronizado(any(), any()) }
    }

    @Test
    fun sincronizar_servidorRespondeError_quedaPendiente() = runTest {
        coEvery { dao.obtenerPendientes(1) } returns listOf(predio, predio.copy(id = 4, remoteId = 51))
        coEvery { api.crear(any()) } returns error(400)
        coEvery { api.actualizar(51, any()) } returns error(500)

        assertFalse(repository.sincronizarPendientes())
        coVerify(exactly = 0) { dao.marcarSincronizado(any(), any()) }
    }

    @Test
    fun sincronizar_predioEditado_loActualiza() = runTest {
        coEvery { dao.obtenerPendientes(1) } returns listOf(predio.copy(remoteId = 50))
        coEvery { api.actualizar(50, any()) } returns Response.success(dto(50))

        assertTrue(repository.sincronizarPendientes())
        coVerify { dao.marcarSincronizado(3, 50) }
    }

    @Test
    fun sincronizar_predioBorrado_avisaAlServidorYLoBorraDelTelefono() = runTest {
        coEvery { dao.obtenerPendientes(1) } returns listOf(
            predio.copy(remoteId = 50, eliminado = true),   // estaba en el servidor
            predio.copy(id = 4, eliminado = true),          // nunca llegó al servidor
            predio.copy(id = 5, remoteId = 52, eliminado = true)
        )
        coEvery { api.eliminar(50) } returns Response.success(Unit)
        coEvery { api.eliminar(52) } returns error(404)    // el servidor ya no lo tenía: da lo mismo

        assertTrue(repository.sincronizarPendientes())
        coVerify { dao.borrarDefinitivo(3) }
        coVerify { dao.borrarDefinitivo(4) }
        coVerify { dao.borrarDefinitivo(5) }
        coVerify(exactly = 2) { api.eliminar(any()) }
    }

    @Test
    fun sincronizar_borradoConErrorDelServidor_noLoBorraDelTelefono() = runTest {
        coEvery { dao.obtenerPendientes(1) } returns listOf(predio.copy(remoteId = 50, eliminado = true))
        coEvery { api.eliminar(50) } returns error(500)

        assertFalse(repository.sincronizarPendientes())
        coVerify(exactly = 0) { dao.borrarDefinitivo(any()) }
    }

    @Test
    fun descargar_agregaLosQueFaltanYActualizaLosSincronizados() = runTest {
        coEvery { api.listar(1) } returns Response.success(listOf(dto(50), dto(51), dto(52), PredioDto(null, 1, "x", "y", superficieHa = 1.0)))
        coEvery { dao.obtenerPorRemoteId(50) } returns null                                              // no está: se agrega
        coEvery { dao.obtenerPorRemoteId(51) } returns predio.copy(id = 7, remoteId = 51, sincronizado = true, fotoUri = "foto")
        coEvery { dao.obtenerPorRemoteId(52) } returns predio.copy(id = 8, remoteId = 52, sincronizado = false) // cambios locales: no se pisa
        val guardados = mutableListOf<PredioEntity>()
        coEvery { dao.guardar(capture(guardados)) } returns 1L

        assertTrue(repository.descargar())

        assertEquals(2, guardados.size)
        assertEquals(50L, guardados[0].remoteId)
        assertEquals(7, guardados[1].id)
        assertEquals("foto", guardados[1].fotoUri)    // la foto solo vive en el teléfono
    }

    @Test
    fun descargar_sinRedOConError_devuelveFalse() = runTest {
        coEvery { api.listar(1) } throws IOException("sin red")
        assertFalse(repository.descargar())

        coEvery { api.listar(1) } returns error(500)
        assertFalse(repository.descargar())
    }
}
