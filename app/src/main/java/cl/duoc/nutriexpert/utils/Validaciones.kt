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