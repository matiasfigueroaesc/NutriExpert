
package cl.duoc.nutriexpert.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planes")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sectorId: Int,
    val nombreTemporada: String,
    val fechaInicio: String? = null,
    val fechaTermino: String? = null,
    val remoteId: Long? = null,
    val sincronizado: Boolean = false,
    val pendienteEliminar: Boolean = false,
    val detalleJson: String? = null
)

