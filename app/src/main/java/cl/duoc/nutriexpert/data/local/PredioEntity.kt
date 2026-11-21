package cl.duoc.nutriexpert.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Tabla "predios" de la base de datos local (Room).
// Cada objeto PredioEntity es una fila de la tabla.
// usuarioId indica el dueño: cada usuario ve solo sus propios predios.
@Entity(
    tableName = "predios",
    indices = [Index(value = ["usuarioId"])]
)
data class PredioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,               // Room asigna el id solo al insertar
    val usuarioId: Int = 0,        // dueño del predio (lo completa PredioRepository)
    val nombre: String,            // ej: "Predio Los Aromos"
    val comuna: String,            // ej: "Quillota"
    val hectareas: Double,         // superficie total en ha
    // Campos para los recursos nativos (los completa la parte de Fabián)
    val latitud: Double? = null,   // ubicación GPS del predio
    val longitud: Double? = null,
    val fotoUri: String? = null,   // ruta de la foto tomada con la cámara
    // Sincronización con el microservicio predios (offline-first)
    val remoteId: Long? = null,        // id que le puso el servidor (null = todavía no llega allá)
    val sincronizado: Boolean = false, // false = tiene cambios que el servidor no conoce
    val eliminado: Boolean = false     // true = se borró sin red; falta avisarle al servidor
)
