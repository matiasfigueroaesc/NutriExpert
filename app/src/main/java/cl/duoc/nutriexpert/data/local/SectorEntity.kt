package cl.duoc.nutriexpert.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "sectores": cada predio se divide en sectores con un cultivo.
@Entity(tableName = "sectores")
data class SectorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val predioId: Int,                 // id LOCAL del predio (el de Room)
    val nombre: String,
    val cultivoId: String,
    val hectareas: Double,
    // Sincronización con el microservicio predios (igual que en PredioEntity)
    val remoteId: Long? = null,
    val sincronizado: Boolean = false,
    val eliminado: Boolean = false
)
