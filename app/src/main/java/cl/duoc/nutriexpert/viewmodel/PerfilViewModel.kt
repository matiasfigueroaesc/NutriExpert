package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.nutriexpert.data.repository.AuthRepository
import cl.duoc.nutriexpert.data.repository.Repositorios
import cl.duoc.nutriexpert.data.repository.ResultadoAuth
import kotlinx.coroutines.launch

// Lo que muestra la pantalla Mi perfil
data class PerfilState(
    val nombre: String = "",            // nombre guardado
    val correo: String = "",
    val nombreEditado: String = "",     // lo que se escribe en el campo
    val errorNombre: String? = null,    // error bajo el campo nombre
    val mensaje: String = "",           // aviso de éxito ("Nombre actualizado")
    val mensajeError: String = "",      // error general (sin internet, servidor caído)
    val guardando: Boolean = false,
    val borrando: Boolean = false,
    val salir: Boolean = false          // true al cerrar sesión o borrar la cuenta: vuelve al login
)

// Ver los datos del usuario, cambiar el nombre, cerrar sesión y borrar la cuenta.
// El AuthRepository llega por el constructor para poder testearlo con MockK.
class PerfilViewModel @JvmOverloads constructor(
    application: Application,
    private val authRepository: AuthRepository = Repositorios.auth(application)
) : AndroidViewModel(application) {

    var estado by mutableStateOf(PerfilState())
        private set

    init {
        // Los datos se leen de la copia local: se ven aunque no haya internet
        viewModelScope.launch {
            authRepository.usuarioActual()?.let { usuario ->
                estado = estado.copy(
                    nombre = usuario.nombre,
                    correo = usuario.correo,
                    nombreEditado = usuario.nombre
                )
            }
        }
    }

    fun onNombreChange(valor: String) {
        estado = estado.copy(nombreEditado = valor, errorNombre = null, mensaje = "", mensajeError = "")
    }

    fun guardarNombre() {
        if (estado.guardando) return

        val nombre = estado.nombreEditado.trim()
        val error = when {
            nombre.isBlank() -> "Ingresa tu nombre"
            nombre.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
        }
        if (error != null) {
            estado = estado.copy(errorNombre = error)
            return
        }
        if (nombre == estado.nombre) {
            estado = estado.copy(mensaje = "No hay cambios que guardar")
            return
        }

        estado = estado.copy(guardando = true, mensaje = "", mensajeError = "")
        viewModelScope.launch {
            estado = when (val resultado = authRepository.actualizarNombre(nombre)) {
                is ResultadoAuth.Exito -> estado.copy(
                    nombre = resultado.usuario.nombre,
                    nombreEditado = resultado.usuario.nombre,
                    mensaje = "Nombre actualizado",
                    guardando = false
                )
                is ResultadoAuth.Error -> estado.copy(mensajeError = resultado.mensaje, guardando = false)
            }
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        estado = estado.copy(salir = true)
    }

    fun borrarCuenta() {
        if (estado.borrando) return

        estado = estado.copy(borrando = true, mensaje = "", mensajeError = "")
        viewModelScope.launch {
            val error = authRepository.eliminarCuenta()
            estado = if (error == null) {
                estado.copy(borrando = false, salir = true)
            } else {
                estado.copy(borrando = false, mensajeError = error)
            }
        }
    }
}
