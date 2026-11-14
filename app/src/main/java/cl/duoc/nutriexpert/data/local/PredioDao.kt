package cl.duoc.nutriexpert.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO: aquí se escriben las consultas a la tabla "predios".
// Room genera el código de cada función automáticamente.
// Todas las consultas filtran por usuarioId: nadie puede ver ni borrar predios ajenos.
@Dao
interface PredioDao {

    // Flow: la lista se actualiza sola cuando cambia la tabla
    @Query("SELECT * FROM predios WHERE usuarioId = :usuarioId ORDER BY nombre")
    fun obtenerPorUsuario(usuarioId: Int): Flow<List<PredioEntity>>

    @Query("SELECT * FROM predios WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun obtenerPorId(id: Int, usuarioId: Int): PredioEntity?

    // REPLACE: si el id ya existe, lo actualiza (sirve para crear y editar)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(predio: PredioEntity)

    @Query("DELETE FROM predios WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun eliminar(id: Int, usuarioId: Int)
}
