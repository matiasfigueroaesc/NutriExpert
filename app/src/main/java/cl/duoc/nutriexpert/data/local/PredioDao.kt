package cl.duoc.nutriexpert.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO: aquí se escriben las consultas a la tabla "predios".
// Room genera el código de cada función automáticamente.
// Las consultas de pantalla filtran por usuarioId: nadie puede ver ni borrar predios ajenos.
// "eliminado = 0" esconde los predios borrados que todavía no se le avisan al servidor.
@Dao
interface PredioDao {

    // Flow: la lista se actualiza sola cuando cambia la tabla
    @Query("SELECT * FROM predios WHERE usuarioId = :usuarioId AND eliminado = 0 ORDER BY nombre")
    fun obtenerPorUsuario(usuarioId: Int): Flow<List<PredioEntity>>

    @Query("SELECT * FROM predios WHERE id = :id AND usuarioId = :usuarioId AND eliminado = 0")
    suspend fun obtenerPorId(id: Int, usuarioId: Int): PredioEntity?

    // REPLACE: si el id ya existe, lo actualiza (sirve para crear y editar).
    // Devuelve el id de la fila guardada.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(predio: PredioEntity): Long

    // Borrar sin red: se marca y se esconde. El SyncWorker lo borra de verdad después.
    @Query("UPDATE predios SET eliminado = 1, sincronizado = 0 WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun marcarEliminado(id: Int, usuarioId: Int)

    // Borrado definitivo, cuando el servidor ya sabe
    @Query("DELETE FROM predios WHERE id = :id")
    suspend fun borrarDefinitivo(id: Int)

    // --- Consultas para la sincronización ---

    // Lo que el servidor todavía no conoce (nuevo, editado o borrado)
    @Query("SELECT * FROM predios WHERE usuarioId = :usuarioId AND sincronizado = 0")
    suspend fun obtenerPendientes(usuarioId: Int): List<PredioEntity>

    @Query("UPDATE predios SET remoteId = :remoteId, sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: Int, remoteId: Long)

    @Query("SELECT * FROM predios WHERE remoteId = :remoteId LIMIT 1")
    suspend fun obtenerPorRemoteId(remoteId: Long): PredioEntity?

    // Los sectores necesitan el id del predio en el servidor
    @Query("SELECT remoteId FROM predios WHERE id = :id")
    suspend fun obtenerRemoteId(id: Int): Long?
}
