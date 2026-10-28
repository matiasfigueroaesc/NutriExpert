
package cl.duoc.nutriexpert.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalisisDao {

    @Query("SELECT * FROM analisis WHERE sectorId = :sectorId ORDER BY fecha DESC")
    fun obtenerPorSector(sectorId: Int): Flow<List<AnalisisEntity>>

    @Query("SELECT * FROM analisis WHERE id = :id")
    suspend fun obtenerPorId(id: Int): AnalisisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(analisis: AnalisisEntity)

    @Delete
    suspend fun eliminar(analisis: AnalisisEntity)
}
