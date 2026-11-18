package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.PredioDao
import cl.duoc.nutriexpert.data.local.SectorDao
import cl.duoc.nutriexpert.data.local.SectorEntity
import cl.duoc.nutriexpert.data.remote.SectorApiService
import cl.duoc.nutriexpert.data.remote.aDto
import cl.duoc.nutriexpert.data.remote.aEntity
import kotlinx.coroutines.flow.Flow
import java.io.IOException

// Repositorio de sectores. Funciona igual que PredioRepository (primero Room, después el servidor).
// Diferencia: un sector solo se puede enviar cuando su predio ya está en el servidor,
// porque el servidor necesita el id del predio que ÉL conoce (remoteId).
class SectorRepository(
    private val dao: SectorDao,
    private val predioDao: PredioDao,
    private val api: SectorApiService
) {

    fun obtenerPorPredio(predioId: Int): Flow<List<SectorEntity>> =
        dao.obtenerPorPredio(predioId)

    suspend fun obtenerPorId(id: Int): SectorEntity? =
        dao.obtenerPorId(id)

    suspend fun guardar(sector: SectorEntity) {
        val anterior = if (sector.id != 0) dao.obtenerPorId(sector.id) else null
        dao.guardar(
            sector.copy(
                remoteId = anterior?.remoteId,
                sincronizado = false,
                eliminado = false
            )
        )
    }

    suspend fun eliminar(sector: SectorEntity) =
        dao.marcarEliminado(sector.id)

    suspend fun sincronizarPendientes(): Boolean {
        var todoOk = true
        for (sector in dao.obtenerPendientes()) {
            if (!enviar(sector)) todoOk = false
        }
        return todoOk
    }

    // Trae del servidor los sectores de un predio que el teléfono no tiene
    suspend fun descargar(predioLocalId: Int): Boolean {
        val predioRemoteId = predioDao.obtenerRemoteId(predioLocalId) ?: return false
        try {
            val respuesta = api.listar(predioRemoteId)
            if (!respuesta.isSuccessful) return false

            for (dto in respuesta.body().orEmpty()) {
                val remoteId = dto.id ?: continue
                val local = dao.obtenerPorRemoteId(remoteId)
                if (local == null) {
                    dao.guardar(dto.aEntity(predioLocalId))
                } else if (local.sincronizado) {
                    dao.guardar(dto.aEntity(predioLocalId).copy(id = local.id))
                }
            }
            return true
        } catch (e: IOException) {
            return false
        }
    }

    private suspend fun enviar(sector: SectorEntity): Boolean {
        try {
            val remoteId = sector.remoteId

            if (sector.eliminado) {
                if (remoteId != null) {
                    val respuesta = api.eliminar(remoteId)
                    if (!respuesta.isSuccessful && respuesta.code() != 404) return false
                }
                dao.borrarDefinitivo(sector.id)
                return true
            }

            // El predio todavía no llega al servidor: este sector espera su turno
            val predioRemoteId = predioDao.obtenerRemoteId(sector.predioId) ?: return false

            if (remoteId == null) {
                val respuesta = api.crear(sector.aDto(predioRemoteId))
                val creado = respuesta.body()
                if (respuesta.isSuccessful && creado?.id != null) {
                    dao.marcarSincronizado(sector.id, creado.id)
                    return true
                }
                return false
            }

            val respuesta = api.actualizar(remoteId, sector.aDto(predioRemoteId))
            if (respuesta.isSuccessful) {
                dao.marcarSincronizado(sector.id, remoteId)
                return true
            }
            return false
        } catch (e: IOException) {
            return false
        }
    }
}
