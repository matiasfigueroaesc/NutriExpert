package cl.duoc.nutriexpert.seguridad

import android.content.Context

/**
 * Guarda quién inició sesión y los intentos fallidos de ingreso.
 *
 * Usa SharedPreferences privadas de la app (ninguna otra app puede leerlas).
 * Solo se guarda el id del usuario, nunca la contraseña.
 */
class SesionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    /** Id del usuario con sesión iniciada, o null si nadie ha ingresado. */
    fun usuarioId(): Int? = prefs.getInt(CLAVE_USUARIO, SIN_USUARIO).takeIf { it > 0 }

    fun haySesion(): Boolean = usuarioId() != null

    fun iniciar(usuarioId: Int) {
        prefs.edit().putInt(CLAVE_USUARIO, usuarioId).apply()
    }

    fun cerrar() {
        prefs.edit().remove(CLAVE_USUARIO).apply()
    }

    // --- Protección contra fuerza bruta en el login ---

    fun intentosFallidos(): Int = prefs.getInt(CLAVE_INTENTOS, 0)

    fun bloqueadoHasta(): Long = prefs.getLong(CLAVE_BLOQUEO, 0L)

    fun guardarIntentos(intentos: Int, bloqueadoHasta: Long) {
        prefs.edit()
            .putInt(CLAVE_INTENTOS, intentos)
            .putLong(CLAVE_BLOQUEO, bloqueadoHasta)
            .apply()
    }

    fun reiniciarIntentos() = guardarIntentos(0, 0L)

    private companion object {
        const val ARCHIVO = "nutriexpert_sesion"
        const val CLAVE_USUARIO = "usuario_id"
        const val CLAVE_INTENTOS = "intentos_fallidos"
        const val CLAVE_BLOQUEO = "bloqueado_hasta"
        const val SIN_USUARIO = -1
    }
}
