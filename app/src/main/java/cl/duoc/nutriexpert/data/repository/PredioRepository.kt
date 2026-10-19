package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.PredioDao
import cl.duoc.nutriexpert.data.local.PredioEntity
import kotlinx.coroutines.flow.Flow

// Repositorio: único punto por donde el ViewModel pide o guarda predios.
// Hoy solo usa Room; más adelante aquí se agrega la sincronización con el backend.
class PredioRepository(private val dao: PredioDao) {

    val predios: Flow<List<PredioEntity>> = dao.obtenerTodos()

    suspend fun obtenerPorId(id: Int): PredioEntity? = dao.obtenerPorId(id)

    suspend fun guardar(predio: PredioEntity) = dao.guardar(predio)

    suspend fun eliminar(predio: PredioEntity) = dao.eliminar(predio)
}
