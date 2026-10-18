
package cl.duoc.nutriexpert.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.nutriexpert.utils.validarRegistro

// Guarda los datos y el resultado del formulario de registro
data class RegistroState(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val aceptaPrivacidad: Boolean = false,
    val mensajeError: String = "",
    val mensajeInformativo: String = ""
)

// Administra los datos y las acciones del registro
class RegistroViewModel : ViewModel() {

    var estado by mutableStateOf(RegistroState())
        private set

    fun onNombreChange(valor: String) {
        estado = estado.copy(
            nombre = valor,
            mensajeError = "",
            mensajeInformativo = ""
        )
    }

    fun onCorreoChange(valor: String) {
        estado = estado.copy(
            correo = valor,
            mensajeError = "",
            mensajeInformativo = ""
        )
    }

    fun onContrasenaChange(valor: String) {
        estado = estado.copy(
            contrasena = valor,
            mensajeError = "",
            mensajeInformativo = ""
        )
    }

    fun onConfirmarContrasenaChange(valor: String) {
        estado = estado.copy(
            confirmarContrasena = valor,
            mensajeError = "",
            mensajeInformativo = ""
        )
    }

    fun onAceptaPrivacidadChange(valor: Boolean) {
        estado = estado.copy(
            aceptaPrivacidad = valor,
            mensajeError = "",
            mensajeInformativo = ""
        )
    }

    // Utiliza las validaciones existentes del proyecto
    fun registrar() {

        val error = validarRegistro(
            nombre = estado.nombre,
            correo = estado.correo,
            contrasena = estado.contrasena,
            confirmarContrasena = estado.confirmarContrasena,
            aceptaPrivacidad = estado.aceptaPrivacidad
        )

        if (error.isNotEmpty()) {
            estado = estado.copy(
                mensajeError = error,
                mensajeInformativo = ""
            )
            return
        }

        // El backend de registro todavía no está implementado
        estado = estado.copy(
            mensajeError = "",
            mensajeInformativo =
                "Formulario válido. El registro aún no está disponible."
        )
    }
}
