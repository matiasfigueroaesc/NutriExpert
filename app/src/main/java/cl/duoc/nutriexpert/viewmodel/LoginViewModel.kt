
package cl.duoc.nutriexpert.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.nutriexpert.utils.ErroresLogin
import cl.duoc.nutriexpert.utils.validarCamposLogin
import cl.duoc.nutriexpert.utils.validarLogin

// Guarda los datos y los resultados del formulario de login
data class LoginState(
    val correo: String = "",
    val contrasena: String = "",
    val errores: ErroresLogin = ErroresLogin(),
    val mensajeError: String = "",
    val ingresoCorrecto: Boolean = false
)

// Administra el estado y las acciones del inicio de sesión
class LoginViewModel : ViewModel() {

    var estado by mutableStateOf(LoginState())
        private set

    // Actualiza el correo y limpia su mensaje de error
    fun onCorreoChange(valor: String) {
        estado = estado.copy(
            correo = valor,
            errores = estado.errores.copy(correo = null),
            mensajeError = "",
            ingresoCorrecto = false
        )
    }

    // Actualiza la contraseña y limpia su mensaje de error
    fun onContrasenaChange(valor: String) {
        estado = estado.copy(
            contrasena = valor,
            errores = estado.errores.copy(contrasena = null),
            mensajeError = "",
            ingresoCorrecto = false
        )
    }

    // Valida los campos para permitir el acceso al prototipo
    fun ingresar() {

        val errores = validarCamposLogin(
            estado.correo,
            estado.contrasena
        )

        // Si existen errores, los guarda para mostrarlos en pantalla
        if (errores.hayErrores()) {
            estado = estado.copy(
                errores = errores,
                mensajeError = "",
                ingresoCorrecto = false
            )
            return
        }

        // Si los campos son válidos, comprueba el resultado del login
        val mensaje = validarLogin(
            estado.correo,
            estado.contrasena
        )

        estado = estado.copy(
            errores = ErroresLogin(),
            mensajeError = mensaje,
            ingresoCorrecto = mensaje.isEmpty()
        )
    }
}
