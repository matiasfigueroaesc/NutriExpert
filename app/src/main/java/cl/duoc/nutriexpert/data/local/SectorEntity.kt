
package cl.duoc.nutriexpert.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sectores")
data class SectorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val predioId: Int,
    val nombre: String,
    val cultivoId: String,
    val hectareas: Double
)
