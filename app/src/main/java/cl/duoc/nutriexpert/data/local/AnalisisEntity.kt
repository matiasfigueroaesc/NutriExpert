
package cl.duoc.nutriexpert.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analisis")
data class AnalisisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val sectorId: Int,
    val fecha: String,

    // Características del suelo
    val profundidadCm: Double = 30.0,
    val densidadAparente: Double = 1.3,
    val pedregosidadPct: Double = 0.0,

    // Nutrientes del suelo (ppm)
    val sueloN: Double,
    val sueloP: Double,
    val sueloK: Double,
    val sueloCa: Double,
    val sueloMg: Double,

    // Nutrientes del agua (mg/L)
    val aguaN: Double,
    val aguaP: Double,
    val aguaK: Double,
    val aguaCa: Double,
    val aguaMg: Double,

    // Volumen de agua (m³/ha)
    val aguaM3Ha: Double
)
