
package cl.duoc.nutriexpert.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SectorDao {

    @androidx.room.Query("UPDATE sectores SET hectareas = :hectareas WHERE id = :sectorId")
    suspend fun actualizarHectareas(sectorId: Int, hectareas: Double): Int

    @Query("SELECT * FROM sectores WHERE predioId = :predioId ORDER BY nombre")
    fun obtenerPorPredio(predioId: Int): Flow<List<SectorEntity>>

    @Query("SELECT * FROM sectores WHERE id = :id")
    suspend fun obtenerPorId(id: Int): SectorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(sector: SectorEntity)

    @Delete
    suspend fun eliminar(sector: SectorEntity)
}
