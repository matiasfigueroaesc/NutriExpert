
package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.nutriexpert.data.repository.AuthRepository
import cl.duoc.nutriexpert.data.repository.Repositorios
import cl.duoc.nutriexpert.data.repository.ResultadoAuth
import cl.duoc.nutriexpert.utils.ErroresLogin
import cl.duoc.nutriexpert.utils.validarCamposLogin
import kotlinx.coroutines.launch

// Guarda los datos y los resultados del formulario de login
data class LoginState(
    val correo: String = "",
    val contrasena: String = "",
    val errores: ErroresLogin = ErroresLogin(),
    val mensajeError: String = "",
    val cargando: Boolean = false,      // true mientras se verifica la contraseña
    val ingresoCorrecto: Boolean = false
)

// Administra el estado y las acciones del inicio de sesión.
// El AuthRepository llega por el constructor para poder testearlo con MockK (igual que PrediosViewModel).
class LoginViewModel @JvmOverloads constructor(
    application: Application,
    private val authRepository: AuthRepository = Repositorios.auth(application)
) : AndroidViewModel(application) {

    var estado by mutableStateOf(LoginState())
        private set

    // Actualiza el correo y limpia su mensaje de error
    fun onCorreoChange(valor: String) {
        estado = estado.copy(
            correo = valor,
            errores = estado.errores.copy(correo = null),
            mensajeError = ""
        )
    }

    // Actualiza la contraseña y limpia su mensaje de error
    fun onContrasenaChange(valor: String) {
        estado = estado.copy(
            contrasena = valor,
            errores = estado.errores.copy(contrasena = null),
            mensajeError = ""
        )
    }

    // Valida los campos y comprueba el correo y la contraseña contra las cuentas guardadas
    fun ingresar() {
        if (estado.cargando) return

        val errores = validarCamposLogin(estado.correo, estado.contrasena)

        // Si existen errores, los guarda para mostrarlos en pantalla
        if (errores.hayErrores()) {
            estado = estado.copy(errores = errores, mensajeError = "")
            return
        }

        estado = estado.copy(errores = ErroresLogin(), mensajeError = "", cargando = true)

        viewModelScope.launch {
            estado = when (val resultado = authRepository.iniciarSesion(estado.correo, estado.contrasena)) {
                is ResultadoAuth.Exito -> estado.copy(
                    contrasena = "",            // no se deja la contraseña en memoria
                    cargando = false,
                    ingresoCorrecto = true
                )
                is ResultadoAuth.Error -> estado.copy(
                    mensajeError = resultado.mensaje,
                    cargando = false
                )
            }
        }
    }
}
