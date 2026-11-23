package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.PredioDao
import cl.duoc.nutriexpert.data.local.PredioEntity
import cl.duoc.nutriexpert.data.local.SectorDao
import cl.duoc.nutriexpert.data.remote.PredioApiService
import cl.duoc.nutriexpert.data.remote.aDto
import cl.duoc.nutriexpert.data.remote.aEntity
import kotlinx.coroutines.flow.Flow
import java.io.IOException

// Repositorio: único punto por donde el ViewModel pide o guarda predios.
// Trabaja siempre con los predios del usuario que inició sesión (usuarioId).
//
// Offline-first, como una libreta de pedidos:
// 1. guardar() y eliminar() anotan el cambio en Room (la libreta) con sincronizado = false.
//    Eso funciona siempre, haya o no internet, y la pantalla se actualiza al tiro.
// 2. sincronizarPendientes() (lo llama el SyncWorker) le lleva al servidor lo anotado.
//    Si no hay red, no pasa nada: queda anotado y se intenta de nuevo después.
class PredioRepository(
    private val dao: PredioDao,
    private val sectorDao: SectorDao,
    private val api: PredioApiService,
    private val usuarioId: Int
) {

    val predios: Flow<List<PredioEntity>> = dao.obtenerPorUsuario(usuarioId)

    suspend fun obtenerPorId(id: Int): PredioEntity? = dao.obtenerPorId(id, usuarioId)

    // El dueño lo pone el repositorio, no la pantalla.
    // Al editar se mantiene el remoteId, para que el servidor actualice y no cree uno nuevo.
    suspend fun guardar(predio: PredioEntity) {
        val anterior = if (predio.id != 0) dao.obtenerPorId(predio.id, usuarioId) else null
        dao.guardar(
            predio.copy(
                usuarioId = usuarioId,
                remoteId = anterior?.remoteId,
                sincronizado = false,
                eliminado = false
            )
        )
    }

    // Se esconde el predio y se borran sus sectores del teléfono.
    // El borrado en el servidor lo hace sincronizarPendientes().
    suspend fun eliminar(predio: PredioEntity) {
        dao.marcarEliminado(predio.id, usuarioId)
        sectorDao.borrarPorPredio(predio.id)
    }

    // Envía al servidor todo lo pendiente. Devuelve true si quedó todo al día.
    suspend fun sincronizarPendientes(): Boolean {
        var todoOk = true
        for (predio in dao.obtenerPendientes(usuarioId)) {
            if (!enviar(predio)) todoOk = false
        }
        return todoOk
    }

    // Trae del servidor los predios que el teléfono no tiene (por ejemplo, si se reinstaló la app)
    // y actualiza los que no tienen cambios locales pendientes.
    suspend fun descargar(): Boolean {
        try {
            val respuesta = api.listar(usuarioId.toLong())
            if (!respuesta.isSuccessful) return false

            for (dto in respuesta.body().orEmpty()) {
                val remoteId = dto.id ?: continue
                val local = dao.obtenerPorRemoteId(remoteId)
                if (local == null) {
                    dao.guardar(dto.aEntity(usuarioId))
                } else if (local.sincronizado) {
                    // La foto solo vive en el teléfono: se conserva
                    dao.guardar(dto.aEntity(usuarioId).copy(id = local.id, fotoUri = local.fotoUri))
                }
            }
            return true
        } catch (e: IOException) {
            return false // sin red
        }
    }

    // Manda UN predio al servidor según lo que le pasó: borrado, nuevo o editado.
    private suspend fun enviar(predio: PredioEntity): Boolean {
        try {
            val remoteId = predio.remoteId

            // Borrado: se avisa al servidor y recién ahí se borra del teléfono
            if (predio.eliminado) {
                if (remoteId != null) {
                    val respuesta = api.eliminar(remoteId)
                    // 404 = el servidor ya no lo tenía, da lo mismo
                    if (!respuesta.isSuccessful && respuesta.code() != 404) return false
                }
                dao.borrarDefinitivo(predio.id)
                return true
            }

            // Nuevo: el servidor responde con su id y lo guardamos como remoteId
            if (remoteId == null) {
                val respuesta = api.crear(predio.aDto())
                val creado = respuesta.body()
                if (respuesta.isSuccessful && creado?.id != null) {
                    dao.marcarSincronizado(predio.id, creado.id)
                    return true
                }
                return false
            }

            // Editado: se actualiza el que ya existe en el servidor
            val respuesta = api.actualizar(remoteId, predio.aDto())
            if (respuesta.isSuccessful) {
                dao.marcarSincronizado(predio.id, remoteId)
                return true
            }
            return false
        } catch (e: IOException) {
            // Sin red o servidor apagado: queda pendiente para el próximo intento
            return false
        }
    }
}
