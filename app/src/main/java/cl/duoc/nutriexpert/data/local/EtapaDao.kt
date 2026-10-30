package cl.duoc.nutriexpert.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EtapaDao {

    // Etapas de un cultivo, en orden
    @Query("SELECT * FROM etapas WHERE cultivoId = :cultivoId ORDER BY orden")
    fun obtenerDeCultivo(cultivoId: String): Flow<List<EtapaEntity>>

    // REPLACE + índice único (cultivoId, orden): recargar no duplica etapas
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(etapas: List<EtapaEntity>)
}
