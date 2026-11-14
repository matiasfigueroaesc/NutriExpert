
package cl.duoc.nutriexpert.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.nutriexpert.data.local.AppDatabase
import cl.duoc.nutriexpert.data.repository.AuthRepository
import cl.duoc.nutriexpert.data.repository.ResultadoAuth
import cl.duoc.nutriexpert.seguridad.SesionManager
import cl.duoc.nutriexpert.utils.ErroresRegistro
import cl.duoc.nutriexpert.utils.validarCamposRegistro
import kotlinx.coroutines.launch

// Guarda los datos y el resultado del formulario de registro
data class RegistroState(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val aceptaPrivacidad: Boolean = false,
    val errores: ErroresRegistro = ErroresRegistro(),
    val mensajeError: String = "",
    val cargando: Boolean = false,       // true mientras se crea la cuenta
    val registroCorrecto: Boolean = false
)

// Administra los datos y las acciones del registro
class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(
        AppDatabase.obtener(application).usuarioDao(),
        SesionManager(application)
    )

    var estado by mutableStateOf(RegistroState())
        private set

    fun onNombreChange(valor: String) {
        estado = estado.copy(
            nombre = valor,
            errores = estado.errores.copy(nombre = ""),
            mensajeError = ""
        )
    }

    fun onCorreoChange(valor: String) {
        estado = estado.copy(
            correo = valor,
            errores = estado.errores.copy(correo = ""),
            mensajeError = ""
        )
    }

    fun onContrasenaChange(valor: String) {
        estado = estado.copy(
            contrasena = valor,
            errores = estado.errores.copy(
                contrasena = "",
                confirmarContrasena = ""
            ),
            mensajeError = ""
        )
    }

    fun onConfirmarContrasenaChange(valor: String) {
        estado = estado.copy(
            confirmarContrasena = valor,
            errores = estado.errores.copy(confirmarContrasena = ""),
            mensajeError = ""
        )
    }

    fun onAceptaPrivacidadChange(valor: Boolean) {
        estado = estado.copy(
            aceptaPrivacidad = valor,
            errores = estado.errores.copy(privacidad = ""),
            mensajeError = ""
        )
    }

    // Valida cada campo por separado y, si todo está bien, crea la cuenta
    fun registrar() {
        if (estado.cargando) return

        val erroresFormulario = validarCamposRegistro(
            nombre = estado.nombre,
            correo = estado.correo,
            contrasena = estado.contrasena,
            confirmarContrasena = estado.confirmarContrasena,
            aceptaPrivacidad = estado.aceptaPrivacidad
        )

        if (erroresFormulario.hayErrores()) {
            estado = estado.copy(errores = erroresFormulario, mensajeError = "")
            return
        }

        estado = estado.copy(errores = ErroresRegistro(), mensajeError = "", cargando = true)

        viewModelScope.launch {
            val resultado = authRepository.registrar(
                nombre = estado.nombre,
                correo = estado.correo,
                contrasena = estado.contrasena
            )
            estado = when (resultado) {
                is ResultadoAuth.Exito -> estado.copy(
                    contrasena = "",
                    confirmarContrasena = "",
                    cargando = false,
                    registroCorrecto = true
                )
                // El correo ya existe: el error se muestra en el campo correo
                is ResultadoAuth.Error -> estado.copy(
                    errores = estado.errores.copy(correo = resultado.mensaje),
                    cargando = false
                )
            }
        }
    }
}
