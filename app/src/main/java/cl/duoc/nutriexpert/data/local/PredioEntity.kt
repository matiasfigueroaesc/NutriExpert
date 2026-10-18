package cl.duoc.nutriexpert.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "predios" de la base de datos local (Room).
// Cada objeto PredioEntity es una fila de la tabla.
@Entity(tableName = "predios")
data class PredioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,               // Room asigna el id solo al insertar
    val nombre: String,            // ej: "Predio Los Aromos"
    val comuna: String,            // ej: "Quillota"
    val hectareas: Double,         // superficie total en ha
    // Campos para los recursos nativos (los completa la parte de Fabián)
    val latitud: Double? = null,   // ubicación GPS del predio
    val longitud: Double? = null,
    val fotoUri: String? = null    // ruta de la foto tomada con la cámara
)
