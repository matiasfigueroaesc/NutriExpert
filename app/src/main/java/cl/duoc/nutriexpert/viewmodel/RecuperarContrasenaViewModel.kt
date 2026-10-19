
package cl.duoc.nutriexpert.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.nutriexpert.utils.validarCorreoRecuperacion

// Guarda los datos y mensajes de recuperación
data class RecuperarContrasenaState(
    val correo: String = "",
    val mensajeError: String = "",
    val mensajeInformativo: String = ""
)

// Administra el formulario de recuperación
class RecuperarContrasenaViewModel : ViewModel() {

    var estado by mutableStateOf(RecuperarContrasenaState())
        private set

    // Actualiza el correo y limpia los mensajes anteriores
    fun onCorreoChange(valor: String) {
        estado = estado.copy(
            correo = valor,
            mensajeError = "",
            mensajeInformativo = ""
        )
    }

    // Valida el correo ingresado
    fun solicitarRecuperacion() {

        val error = validarCorreoRecuperacion(estado.correo)

        if (error.isNotEmpty()) {
            estado = estado.copy(
                mensajeError = error,
                mensajeInformativo = ""
            )
            return
        }

        // La recuperación real necesita conexión con el servidor
        estado = estado.copy(
            mensajeError = "",
            mensajeInformativo =
                "Correo válido. La recuperación de contraseña estará disponible cuando conectemos el servidor."
        )
    }
}
