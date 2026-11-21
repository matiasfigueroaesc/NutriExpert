package cl.duoc.nutriexpert.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Tabla "usuarios". El correo es único: no puede haber dos cuentas con el mismo.
// La contraseña NO se guarda; solo su hash (ver seguridad/Contrasenas.kt).
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val correo: String,             // siempre en minúsculas y sin espacios
    val hashContrasena: String,     // "pbkdf2:iteraciones:sal:hash"
    val creadoEn: Long = System.currentTimeMillis()
)
