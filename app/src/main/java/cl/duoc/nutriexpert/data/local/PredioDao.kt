package cl.duoc.nutriexpert.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO: aquí se escriben las consultas a la tabla "predios".
// Room genera el código de cada función automáticamente.
@Dao
interface PredioDao {

    // Flow: la lista se actualiza sola cuando cambia la tabla
    @Query("SELECT * FROM predios ORDER BY nombre")
    fun obtenerTodos(): Flow<List<PredioEntity>>

    @Query("SELECT * FROM predios WHERE id = :id")
    suspend fun obtenerPorId(id: Int): PredioEntity?

    // REPLACE: si el id ya existe, lo actualiza (sirve para crear y editar)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(predio: PredioEntity)

    @Delete
    suspend fun eliminar(predio: PredioEntity)
}
