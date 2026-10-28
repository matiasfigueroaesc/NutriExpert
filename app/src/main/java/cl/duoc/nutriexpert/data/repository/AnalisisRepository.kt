
package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.AnalisisDao
import cl.duoc.nutriexpert.data.local.AnalisisEntity
import kotlinx.coroutines.flow.Flow

class AnalisisRepository(private val dao: AnalisisDao) {

    fun obtenerPorSector(sectorId: Int): Flow<List<AnalisisEntity>> =
        dao.obtenerPorSector(sectorId)

    suspend fun obtenerPorId(id: Int): AnalisisEntity? =
        dao.obtenerPorId(id)

    suspend fun guardar(analisis: AnalisisEntity) =
        dao.guardar(analisis)

    suspend fun eliminar(analisis: AnalisisEntity) =
        dao.eliminar(analisis)
}
