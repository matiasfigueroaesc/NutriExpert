
package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.SectorDao
import cl.duoc.nutriexpert.data.local.SectorEntity
import kotlinx.coroutines.flow.Flow

class SectorRepository(private val dao: SectorDao) {

    suspend fun actualizarHectareas(sectorId: Int, hectareas: Double): Int =
        dao.actualizarHectareas(sectorId, hectareas)

    fun obtenerPorPredio(predioId: Int): Flow<List<SectorEntity>> =
        dao.obtenerPorPredio(predioId)

    suspend fun obtenerPorId(id: Int): SectorEntity? =
        dao.obtenerPorId(id)

    suspend fun guardar(sector: SectorEntity) =
        dao.guardar(sector)

    suspend fun eliminar(sector: SectorEntity) =
        dao.eliminar(sector)
}
