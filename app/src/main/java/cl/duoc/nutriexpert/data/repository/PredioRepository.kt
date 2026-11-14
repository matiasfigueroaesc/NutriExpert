package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.PredioDao
import cl.duoc.nutriexpert.data.local.PredioEntity
import kotlinx.coroutines.flow.Flow

// Repositorio: único punto por donde el ViewModel pide o guarda predios.
// Trabaja siempre con los predios del usuario que inició sesión (usuarioId).
// Hoy solo usa Room; más adelante aquí se agrega la sincronización con el backend.
class PredioRepository(
    private val dao: PredioDao,
    private val usuarioId: Int
) {

    val predios: Flow<List<PredioEntity>> = dao.obtenerPorUsuario(usuarioId)

    suspend fun obtenerPorId(id: Int): PredioEntity? = dao.obtenerPorId(id, usuarioId)

    // El dueño lo pone el repositorio, no la pantalla
    suspend fun guardar(predio: PredioEntity) = dao.guardar(predio.copy(usuarioId = usuarioId))

    suspend fun eliminar(predio: PredioEntity) = dao.eliminar(predio.id, usuarioId)
}
