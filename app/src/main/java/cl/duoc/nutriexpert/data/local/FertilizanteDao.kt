package cl.duoc.nutriexpert.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizanteDao {

    @Query("SELECT * FROM fertilizantes ORDER BY nombre")
    fun obtenerTodos(): Flow<List<FertilizanteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(fertilizantes: List<FertilizanteEntity>)
}
