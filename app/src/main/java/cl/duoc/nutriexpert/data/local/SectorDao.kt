package cl.duoc.nutriexpert.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Consultas a la tabla "sectores". Funciona igual que PredioDao.
@Dao
interface SectorDao {

    @Query("SELECT * FROM sectores WHERE predioId = :predioId AND eliminado = 0 ORDER BY nombre")
    fun obtenerPorPredio(predioId: Int): Flow<List<SectorEntity>>

    @Query("SELECT * FROM sectores WHERE id = :id AND eliminado = 0")
    suspend fun obtenerPorId(id: Int): SectorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(sector: SectorEntity): Long

    @Query("UPDATE sectores SET eliminado = 1, sincronizado = 0 WHERE id = :id")
    suspend fun marcarEliminado(id: Int)

    @Query("DELETE FROM sectores WHERE id = :id")
    suspend fun borrarDefinitivo(id: Int)

    // Al borrar un predio se borran sus sectores del teléfono.
    // En el servidor los borra el propio microservicio (borrado en cascada).
    @Query("DELETE FROM sectores WHERE predioId = :predioId")
    suspend fun borrarPorPredio(predioId: Int)

    // --- Consultas para la sincronización ---

    @Query("SELECT * FROM sectores WHERE sincronizado = 0")
    suspend fun obtenerPendientes(): List<SectorEntity>

    @Query("UPDATE sectores SET remoteId = :remoteId, sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: Int, remoteId: Long)

    @Query("SELECT * FROM sectores WHERE remoteId = :remoteId LIMIT 1")
    suspend fun obtenerPorRemoteId(remoteId: Long): SectorEntity?
}
