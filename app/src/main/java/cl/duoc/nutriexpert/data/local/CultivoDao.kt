package cl.duoc.nutriexpert.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CultivoDao {

    // Flow: la lista se actualiza sola (por ejemplo, apenas termina la carga inicial)
    @Query("SELECT * FROM cultivos ORDER BY nombre")
    fun obtenerTodos(): Flow<List<CultivoEntity>>

    @Query("SELECT * FROM cultivos WHERE id = :id")
    suspend fun obtenerPorId(id: String): CultivoEntity?

    // REPLACE: si el cultivo ya existe, lo actualiza con los datos nuevos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(cultivos: List<CultivoEntity>)
}
