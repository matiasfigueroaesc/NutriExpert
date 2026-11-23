package cl.duoc.nutriexpert.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerPorId(id: Int): UsuarioEntity?

    // ABORT: si el correo ya existe, lanza un error en vez de reemplazar la cuenta
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    // REPLACE: crea o actualiza la copia local de un usuario que vino del servidor
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(usuario: UsuarioEntity)

    @Query("UPDATE usuarios SET nombre = :nombre WHERE id = :id")
    suspend fun actualizarNombre(id: Int, nombre: String)

    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun eliminar(id: Int)

    // Cambia el dueño de los predios (de una cuenta local antigua a la cuenta del servidor)
    @Query("UPDATE predios SET usuarioId = :nuevo WHERE usuarioId = :anterior")
    suspend fun traspasarPredios(anterior: Int, nuevo: Int)

    /**
     * Guarda en el teléfono al usuario que respondió el servidor, con el MISMO id del servidor.
     * Así el usuarioId de los predios coincide en el teléfono y en el backend (sincronización).
     *
     * Si en el teléfono ya había una cuenta local con ese correo pero con otro id
     * (creada antes de existir el servidor), sus predios pasan a la cuenta del servidor
     * y la cuenta antigua se borra. Todo en una transacción: o se hace completo o nada.
     */
    @Transaction
    suspend fun guardarDesdeServidor(usuario: UsuarioEntity) {
        val anterior = buscarPorCorreo(usuario.correo)
        if (anterior != null && anterior.id != usuario.id) {
            traspasarPredios(anterior.id, usuario.id)
            eliminar(anterior.id)
        }
        guardar(usuario)
    }
}
