package cl.duoc.nutriexpert.data.repository

import android.database.sqlite.SQLiteConstraintException
import cl.duoc.nutriexpert.data.local.UsuarioDao
import cl.duoc.nutriexpert.data.local.UsuarioEntity
import cl.duoc.nutriexpert.seguridad.Contrasenas
import cl.duoc.nutriexpert.seguridad.SesionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Resultado de registrar o iniciar sesión
sealed class ResultadoAuth {
    data class Exito(val usuario: UsuarioEntity) : ResultadoAuth()
    data class Error(val mensaje: String) : ResultadoAuth()
}

/**
 * Único punto por donde la app crea cuentas, inicia y cierra sesión.
 *
 * Hoy valida contra Room (funciona sin internet). Cuando exista el backend,
 * aquí se agrega la llamada a la API sin tocar las pantallas ni los ViewModels.
 */
class AuthRepository(
    private val dao: UsuarioDao,
    private val sesion: SesionManager,
    private val ahora: () -> Long = { System.currentTimeMillis() }
) {

    suspend fun registrar(nombre: String, correo: String, contrasena: String): ResultadoAuth {
        val correoNormalizado = normalizar(correo)

        if (dao.buscarPorCorreo(correoNormalizado) != null) {
            return ResultadoAuth.Error("Ya existe una cuenta con ese correo")
        }

        // El hash es un cálculo pesado: se hace fuera del hilo principal
        val hash = withContext(Dispatchers.Default) { Contrasenas.generarHash(contrasena) }
        val nuevo = UsuarioEntity(
            nombre = nombre.trim(),
            correo = correoNormalizado,
            hashContrasena = hash
        )

        val id = try {
            dao.insertar(nuevo)
        } catch (e: SQLiteConstraintException) {
            // Otro registro con el mismo correo entró justo antes
            return ResultadoAuth.Error("Ya existe una cuenta con ese correo")
        }

        val usuario = nuevo.copy(id = id.toInt())
        sesion.reiniciarIntentos()
        sesion.iniciar(usuario.id)
        return ResultadoAuth.Exito(usuario)
    }

    suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoAuth {
        val espera = sesion.bloqueadoHasta() - ahora()
        if (espera > 0) {
            return ResultadoAuth.Error(mensajeBloqueo(espera))
        }

        val usuario = dao.buscarPorCorreo(normalizar(correo))

        val correcta = withContext(Dispatchers.Default) {
            if (usuario != null) {
                Contrasenas.verificar(contrasena, usuario.hashContrasena)
            } else {
                // Se calcula un hash igual, para que la respuesta tarde lo mismo
                // exista o no el correo (no revela qué correos están registrados)
                Contrasenas.verificar(contrasena, HASH_FALSO)
                false
            }
        }

        if (usuario == null || !correcta) {
            return registrarFallo()
        }

        sesion.reiniciarIntentos()
        sesion.iniciar(usuario.id)
        return ResultadoAuth.Exito(usuario)
    }

    suspend fun usuarioActual(): UsuarioEntity? =
        sesion.usuarioId()?.let { dao.obtenerPorId(it) }

    fun cerrarSesion() = sesion.cerrar()

    // Tras MAX_INTENTOS errores seguidos, el login se bloquea un rato
    private fun registrarFallo(): ResultadoAuth {
        val intentos = sesion.intentosFallidos() + 1
        return if (intentos >= MAX_INTENTOS) {
            sesion.guardarIntentos(0, ahora() + BLOQUEO_MS)
            ResultadoAuth.Error(mensajeBloqueo(BLOQUEO_MS))
        } else {
            sesion.guardarIntentos(intentos, 0L)
            ResultadoAuth.Error("Correo o contraseña incorrectos")
        }
    }

    private fun mensajeBloqueo(esperaMs: Long): String {
        val segundos = (esperaMs + 999) / 1000
        return "Demasiados intentos fallidos. Espera $segundos segundos e inténtalo de nuevo."
    }

    private fun normalizar(correo: String) = correo.trim().lowercase()

    private companion object {
        const val MAX_INTENTOS = 5
        const val BLOQUEO_MS = 30_000L

        // Hash de una contraseña cualquiera, solo para igualar tiempos de respuesta
        val HASH_FALSO: String by lazy { Contrasenas.generarHash("contrasena-de-relleno") }
    }
}
