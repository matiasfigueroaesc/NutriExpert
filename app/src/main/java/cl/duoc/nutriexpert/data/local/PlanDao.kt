
package cl.duoc.nutriexpert.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Query("SELECT * FROM planes WHERE sectorId = :sectorId AND pendienteEliminar = 0 ORDER BY id DESC")
    fun obtenerPorSector(sectorId: Int): Flow<List<PlanEntity>>

    @Query("SELECT * FROM planes WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): PlanEntity?

    @Query("SELECT * FROM planes WHERE sincronizado = 0")
    suspend fun obtenerPendientes(): List<PlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(plan: PlanEntity): Long

    @Update
    suspend fun actualizar(plan: PlanEntity)

    @Delete
    suspend fun eliminar(plan: PlanEntity)

    @Query("DELETE FROM planes WHERE id = :id")
    suspend fun eliminarPorId(id: Int)
}

