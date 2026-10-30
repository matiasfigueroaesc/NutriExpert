package cl.duoc.nutriexpert.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Tabla "etapas": las etapas de cada cultivo y qué parte de la dosis va en cada una.
// El índice único (cultivoId, orden) evita etapas repetidas al recargar el catálogo.
@Entity(
    tableName = "etapas",
    foreignKeys = [
        ForeignKey(
            entity = CultivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["cultivoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cultivoId", "orden"], unique = true)]
)
data class EtapaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cultivoId: String,      // ej: "cultivo-tomate"
    val orden: Int,             // 1, 2, 3...
    val nombre: String,         // ej: "Cuaja y llenado"
    val semanas: Int,
    val riegosSemana: Int,
    // Porcentaje de la dosis de cada nutriente que se aplica en esta etapa
    val pctN: Double,
    val pctP2o5: Double,
    val pctK2o: Double,
    val pctCao: Double,
    val pctMgo: Double
)
