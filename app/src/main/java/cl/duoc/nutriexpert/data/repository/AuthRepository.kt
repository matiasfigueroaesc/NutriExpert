package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.UsuarioDao
import cl.duoc.nutriexpert.data.local.UsuarioEntity
import cl.duoc.nutriexpert.data.remote.UsuarioApiService
import cl.duoc.nutriexpert.data.remote.UsuarioDto
import cl.duoc.nutriexpert.data.remote.mensajeDeError
import cl.duoc.nutriexpert.seguridad.Contrasenas
import cl.duoc.nutriexpert.seguridad.SesionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

// Resultado de registrar, iniciar sesión o cambiar el nombre
sealed class ResultadoAuth {
    data class Exito(val usuario: UsuarioEntity) : ResultadoAuth()
    data class Error(val mensaje: String) : ResultadoAuth()
}

/**
 * Único punto por donde la app crea cuentas, inicia y cierra sesión.
 *
 * Primero pregunta al microservicio usuarios (en la EC2). Room guarda una copia del usuario,
 * con el MISMO id del servidor y la contraseña en hash PBKDF2, que sirve de respaldo:
 * - Registrarse necesita internet (sin el id del servidor no se podrían sincronizar los predios).
 * - Iniciar sesión funciona sin internet si la persona ya entró antes en este teléfono.
 */
class AuthRepository(
    private val dao: UsuarioDao,
    private val sesion: SesionManager,
    private val api: UsuarioApiService,
    private val ahora: () -> Long = { System.currentTimeMillis() }
) {

    suspend fun registrar(nombre: String, correo: String, contrasena: String): ResultadoAuth {
        val correoNormalizado = normalizar(correo)

        val respuesta = try {
            api.registrar(UsuarioDto(nombre = nombre.trim(), email = correoNormalizado, password = contrasena))
        } catch (e: IOException) {
            return ResultadoAuth.Error(SIN_RED_REGISTRO)
        }

        val remoto = respuesta.body()
        if (!respuesta.isSuccessful || remoto?.id == null) {
            // 400: correo repetido o datos inválidos; el servidor explica cuál
            return ResultadoAuth.Error(respuesta.mensajeDeError() ?: errorServidor(respuesta.code()))
        }

        val usuario = guardarCopiaLocal(remoto, correoNormalizado, contrasena)
        sesion.reiniciarIntentos()
        sesion.iniciar(usuario.id)
        return ResultadoAuth.Exito(usuario)
    }

    suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoAuth {
        val espera = sesion.bloqueadoHasta() - ahora()
        if (espera > 0) {
            return ResultadoAuth.Error(mensajeBloqueo(espera))
        }

        val correoNormalizado = normalizar(correo)

        val respuesta = try {
            api.login(UsuarioDto(email = correoNormalizado, password = contrasena))
        } catch (e: IOException) {
            // Sin señal o servidor apagado: se revisa la copia guardada en el teléfono
            return iniciarSinConexion(correoNormalizado, contrasena)
        }

        val remoto = respuesta.body()
        return when {
            respuesta.isSuccessful && remoto?.id != null -> {
                val usuario = guardarCopiaLocal(remoto, correoNormalizado, contrasena)
                sesion.reiniciarIntentos()
                sesion.iniciar(usuario.id)
                ResultadoAuth.Exito(usuario)
            }
            respuesta.code() == 401 -> registrarFallo()
            respuesta.code() == 400 ->
                ResultadoAuth.Error(respuesta.mensajeDeError() ?: "Faltan el correo o la contraseña")
            // El servidor tuvo un problema (500, 503...): también se usa el respaldo local
            else -> iniciarSinConexion(correoNormalizado, contrasena)
        }
    }

    suspend fun usuarioActual(): UsuarioEntity? =
        sesion.usuarioId()?.let { dao.obtenerPorId(it) }

    fun cerrarSesion() = sesion.cerrar()

    // Cambia el nombre en el servidor y después en la copia local. Necesita internet.
    suspend fun actualizarNombre(nombre: String): ResultadoAuth {
        val id = sesion.usuarioId() ?: return ResultadoAuth.Error(SIN_SESION)

        val respuesta = try {
            api.actualizar(id.toLong(), UsuarioDto(nombre = nombre.trim()))
        } catch (e: IOException) {
            return ResultadoAuth.Error("Necesitas internet para cambiar tu nombre")
        }

        if (!respuesta.isSuccessful) {
            return ResultadoAuth.Error(
                if (respuesta.code() == 404) CUENTA_NO_EXISTE
                else respuesta.mensajeDeError() ?: errorServidor(respuesta.code())
            )
        }

        dao.actualizarNombre(id, respuesta.body()?.nombre ?: nombre.trim())
        val usuario = dao.obtenerPorId(id) ?: return ResultadoAuth.Error(SIN_SESION)
        return ResultadoAuth.Exito(usuario)
    }

    // Borra la cuenta en el servidor y en el teléfono, y cierra la sesión.
    // Devuelve null si salió bien, o el mensaje de error. Necesita internet.
    suspend fun eliminarCuenta(): String? {
        val id = sesion.usuarioId() ?: return SIN_SESION

        val respuesta = try {
            api.eliminar(id.toLong())
        } catch (e: IOException) {
            return "Necesitas internet para borrar tu cuenta"
        }

        // 404 = ya no existía en el servidor: igual se borra la copia local
        if (!respuesta.isSuccessful && respuesta.code() != 404) {
            return respuesta.mensajeDeError() ?: errorServidor(respuesta.code())
        }

        dao.eliminar(id)
        sesion.cerrar()
        return null
    }

    // Login sin internet: compara con el hash PBKDF2 guardado en Room
    private suspend fun iniciarSinConexion(correo: String, contrasena: String): ResultadoAuth {
        val usuario = dao.buscarPorCorreo(correo)
            ?: return ResultadoAuth.Error(SIN_RED_PRIMER_INGRESO)

        val correcta = withContext(Dispatchers.Default) {
            Contrasenas.verificar(contrasena, usuario.hashContrasena)
        }
        if (!correcta) return registrarFallo()

        sesion.reiniciarIntentos()
        sesion.iniciar(usuario.id)
        return ResultadoAuth.Exito(usuario)
    }

    // Guarda el usuario del servidor en Room con su mismo id, y la contraseña en hash
    // para poder entrar después sin internet. El hash es pesado: va fuera del hilo principal.
    private suspend fun guardarCopiaLocal(
        remoto: UsuarioDto,
        correo: String,
        contrasena: String
    ): UsuarioEntity {
        val hash = withContext(Dispatchers.Default) { Contrasenas.generarHash(contrasena) }
        val usuario = UsuarioEntity(
            id = remoto.id!!.toInt(),
            nombre = remoto.nombre.orEmpty(),
            correo = remoto.email?.let { normalizar(it) } ?: correo,
            hashContrasena = hash
        )
        dao.guardarDesdeServidor(usuario)
        return usuario
    }

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

    private fun errorServidor(codigo: Int) =
        "El servidor no pudo responder (código $codigo). Inténtalo más tarde."

    private fun normalizar(correo: String) = correo.trim().lowercase()

    companion object {
        const val MAX_INTENTOS = 5
        const val BLOQUEO_MS = 30_000L

        const val SIN_RED_REGISTRO = "Necesitas internet para crear tu cuenta"
        const val SIN_RED_PRIMER_INGRESO =
            "Sin conexión. Si es tu primer ingreso en este teléfono, conéctate a internet e inténtalo de nuevo."
        const val SIN_SESION = "No hay una sesión iniciada"
        const val CUENTA_NO_EXISTE = "Tu cuenta ya no existe en el servidor"
    }
}
