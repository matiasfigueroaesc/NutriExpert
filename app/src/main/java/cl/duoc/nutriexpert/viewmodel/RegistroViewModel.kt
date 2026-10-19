
package cl.duoc.nutriexpert.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.nutriexpert.utils.ErroresRegistro
import cl.duoc.nutriexpert.utils.validarCamposRegistro

// Guarda los datos y el resultado del formulario de registro
data class RegistroState(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val aceptaPrivacidad: Boolean = false,
    val errores: ErroresRegistro = ErroresRegistro(),
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
            errores = estado.errores.copy(nombre = ""),
            mensajeError = "",
            mensajeInformativo = ""
        )
    }

    fun onCorreoChange(valor: String) {
        estado = estado.copy(
            correo = valor,
            errores = estado.errores.copy(correo = ""),
            mensajeError = "",
            mensajeInformativo = ""
        )
    }

    fun onContrasenaChange(valor: String) {
        estado = estado.copy(
            contrasena = valor,
            errores = estado.errores.copy(
                contrasena = "",
                confirmarContrasena = ""
            ),
            mensajeError = "",
            mensajeInformativo = ""
        )
    }

    fun onConfirmarContrasenaChange(valor: String) {
        estado = estado.copy(
            confirmarContrasena = valor,
            errores = estado.errores.copy(confirmarContrasena = ""),
            mensajeError = "",
            mensajeInformativo = ""
        )
    }

    fun onAceptaPrivacidadChange(valor: Boolean) {
        estado = estado.copy(
            aceptaPrivacidad = valor,
            errores = estado.errores.copy(privacidad = ""),
            mensajeError = "",
            mensajeInformativo = ""
        )
    }

    // Valida cada campo del formulario por separado
    fun registrar() {

        val erroresFormulario = validarCamposRegistro(
            nombre = estado.nombre,
            correo = estado.correo,
            contrasena = estado.contrasena,
            confirmarContrasena = estado.confirmarContrasena,
            aceptaPrivacidad = estado.aceptaPrivacidad
        )

        if (erroresFormulario.hayErrores()) {
            estado = estado.copy(
                errores = erroresFormulario,
                mensajeError = "",
                mensajeInformativo = ""
            )
            return
        }

        // El backend de registro todavía no está implementado
        estado = estado.copy(
            errores = ErroresRegistro(),
            mensajeError = "",
            mensajeInformativo =
                "Formulario válido. El registro aún no está disponible."
        )
    }
}
