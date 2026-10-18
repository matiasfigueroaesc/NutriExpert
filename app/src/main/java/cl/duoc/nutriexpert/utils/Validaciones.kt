package cl.duoc.nutriexpert.utils

// Expresión para comprobar el formato de un correo
private val REGEX_EMAIL =
    Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

// Comprueba si el correo tiene un formato válido
fun esEmailValido(email: String): Boolean {
    return REGEX_EMAIL.matches(email.trim())
}

// Comprueba los datos ingresados en el Login
fun validarLogin(correo: String, contrasena: String): String {
    return when {
        correo.isBlank() -> "Ingresa tu correo"
        !esEmailValido(correo) -> "El correo no tiene un formato válido"
        contrasena.isBlank() -> "Ingresa tu contraseña"
        contrasena.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
        correo.trim() != "prueba@gmail.com" || contrasena != "12345678" ->
            "Correo o contraseña de prueba incorrectos"
        else -> ""
    }

}

fun validarCorreoRecuperacion(correo: String): String {
    return when {
        correo.isBlank() -> "Ingresa tu correo electrónico"
        !esEmailValido(correo) -> "El correo no tiene un formato válido"
        else -> ""
    }
}

fun validarRegistro(
    nombre: String,
    correo: String,
    contrasena: String,
    confirmarContrasena: String,
    aceptaPrivacidad: Boolean
): String {
    return when {
        nombre.isBlank() ->
            "Ingresa tu nombre"

        correo.isBlank() ->
            "Ingresa tu correo"

        !esEmailValido(correo) ->
            "El correo no tiene un formato válido"

        contrasena.isBlank() ->
            "Ingresa tu contraseña"

        contrasena.length < 8 ->
            "La contraseña debe tener al menos 8 caracteres"

        confirmarContrasena != contrasena ->
            "Las contraseñas no coinciden"

        !aceptaPrivacidad ->
            "Debes aceptar la política de privacidad"

        else -> ""
    }
}

// Errores del formulario de predio: un mensaje por campo (null = sin error)
data class ErroresPredio(
    val nombre: String? = null,
    val comuna: String? = null,
    val hectareas: String? = null
) {
    fun hayErrores(): Boolean = nombre != null || comuna != null || hectareas != null
}

// Valida cada campo del predio por separado (RF-09)
fun validarPredio(nombre: String, comuna: String, hectareas: String): ErroresPredio {
    val superficie = hectareas.replace(",", ".").toDoubleOrNull()
    return ErroresPredio(
        nombre = when {
            nombre.isBlank() -> "Ingresa el nombre del predio"
            nombre.trim().length < 3 -> "El nombre debe tener al menos 3 caracteres"
            else -> null
        },
        comuna = if (comuna.isBlank()) "Ingresa la comuna" else null,
        hectareas = when {
            hectareas.isBlank() -> "Ingresa la superficie"
            superficie == null -> "Usa solo números, ej: 7,5"
            superficie <= 0.0 -> "La superficie debe ser mayor a 0"
            superficie > 10000.0 -> "La superficie parece demasiado grande"
            else -> null
        }
    )
}
