
package cl.duoc.nutriexpert.utils

// Expresión para comprobar el formato de un correo
private val REGEX_EMAIL =
    Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

// Comprueba si el correo tiene un formato válido
fun esEmailValido(email: String): Boolean {
    return REGEX_EMAIL.matches(email.trim())
}


// VALIDACIONES DEL LOGIN


// Guarda los errores de cada campo del login
// null significa que no existe un error
data class ErroresLogin(
    val correo: String? = null,
    val contrasena: String? = null
) {
    fun hayErrores(): Boolean =
        correo != null || contrasena != null
}

// Valida el correo y la contraseña por separado
fun validarCamposLogin(
    correo: String,
    contrasena: String
): ErroresLogin {

    return ErroresLogin(
        correo = when {
            correo.isBlank() -> "Ingresa tu correo"
            !esEmailValido(correo) ->
                "El correo no tiene un formato válido"
            else -> null
        },

        contrasena = when {
            contrasena.isBlank() -> "Ingresa tu contraseña"
            contrasena.length < 8 ->
                "La contraseña debe tener al menos 8 caracteres"
            else -> null
        }
    )
}

 // Valida los datos ingresados para permitir el acceso al prototipo
fun validarLogin(
    correo: String,
    contrasena: String
): String {

    val errores = validarCamposLogin(correo, contrasena)

    if (errores.hayErrores()) {
        return errores.correo ?: errores.contrasena ?: ""
    }

    return ""
}


// VALIDACIONES DE RECUPERACIÓN


fun validarCorreoRecuperacion(correo: String): String {
    return when {
        correo.isBlank() -> "Ingresa tu correo electrónico"
        !esEmailValido(correo) -> "El correo no tiene un formato válido"
        else -> ""
    }
}


data class ErroresRegistro(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val privacidad: String = ""
) {
    fun hayErrores(): Boolean =
        nombre.isNotEmpty() ||
                correo.isNotEmpty() ||
                contrasena.isNotEmpty() ||
                confirmarContrasena.isNotEmpty() ||
                privacidad.isNotEmpty()
}

fun validarCamposRegistro(
    nombre: String,
    correo: String,
    contrasena: String,
    confirmarContrasena: String,
    aceptaPrivacidad: Boolean

): ErroresRegistro {

    return ErroresRegistro(
        nombre = when {
            nombre.isBlank() -> "Ingresa tu nombre"
            nombre.trim().length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> ""
        },

        correo = when {
            correo.isBlank() -> "Ingresa tu correo"
            !esEmailValido(correo) ->
                "El correo no tiene un formato válido"
            else -> ""
        },

        contrasena = when {
            contrasena.isBlank() -> "Ingresa tu contraseña"
            contrasena.length < 8 ->
                "La contraseña debe tener al menos 8 caracteres"
            !contrasena.any { it.isLetter() } || !contrasena.any { it.isDigit() } ->
                "La contraseña debe tener letras y números"
            else -> ""
        },

        confirmarContrasena = if (
            confirmarContrasena != contrasena
        ) {
            "Las contraseñas no coinciden"
        } else {
            ""
        },

        privacidad = if (!aceptaPrivacidad) {
            "Debes aceptar la política de privacidad"
        } else {
            ""
        }
    )
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


// VALIDACIONES DE SECTORES

data class ErroresSector(
    val nombre: String? = null,
    val cultivo: String? = null,
    val hectareas: String? = null
) {
    fun hayErrores(): Boolean =
        nombre != null || cultivo != null || hectareas != null
}

fun validarSector(
    nombre: String,
    cultivoId: String,
    hectareas: String
): ErroresSector {

    val superficie = hectareas.trim()
        .replace(",", ".")
        .toDoubleOrNull()

    return ErroresSector(
        nombre = when {
            nombre.isBlank() -> "Ingresa el nombre del sector"
            nombre.trim().length < 3 ->
                "El nombre debe tener al menos 3 caracteres"
            else -> null
        },

        cultivo = if (cultivoId.isBlank()) {
            "Selecciona un cultivo"
        } else {
            null
        },

        hectareas = when {
            hectareas.isBlank() -> "Ingresa la superficie"
            superficie == null || !superficie.isFinite() ->
                "Usa solo números, ej: 7,5"
            superficie <= 0.0 ->
                "La superficie debe ser mayor a 0"
            else -> null
        }
    )
}


// VALIDACIONES DE ANÁLISIS DE SUELO Y AGUA

data class ErroresAnalisis(
    val fecha: String? = null,
    val campos: Map<String, String> = emptyMap()
) {
    fun hayErrores(): Boolean =
        fecha != null || campos.isNotEmpty()
}

fun validarAnalisis(
    fecha: String,
    valores: Map<String, String>
): ErroresAnalisis {

    val errores = mutableMapOf<String, String>()

    val camposObligatorios = listOf(
        "profundidadCm",
        "densidadAparente",
        "pedregosidadPct",
        "sueloN",
        "sueloP",
        "sueloK",
        "sueloCa",
        "sueloMg",
        "aguaN",
        "aguaP",
        "aguaK",
        "aguaCa",
        "aguaMg",
        "aguaM3Ha"
    )

    for (campo in camposObligatorios) {
        val texto = valores[campo].orEmpty().trim()
        val numero = texto.replace(",", ".").toDoubleOrNull()

        errores[campo] = when {
            texto.isBlank() -> "Este campo es obligatorio"
            numero == null || !numero.isFinite() ->
                "Ingresa un número válido"
            numero < 0.0 ->
                "El valor no puede ser negativo"
            campo == "profundidadCm" && numero == 0.0 ->
                "La profundidad debe ser mayor a cero"
            campo == "densidadAparente" && numero == 0.0 ->
                "La densidad debe ser mayor a cero"
            campo == "pedregosidadPct" && numero > 100.0 ->
                "La pedregosidad no puede superar el 100%"
            else -> continue
        }
    }

    val fechaValida = esFechaValida(fecha.trim())

    return ErroresAnalisis(
        fecha = if (fechaValida) null
        else "Ingresa una fecha válida (AAAA-MM-DD)",
        campos = errores
    )
}


// Comprueba una fecha AAAA-MM-DD que exista de verdad (rechaza 2026-02-30).
// Se usa SimpleDateFormat estricto porque java.time recién existe desde Android 8
// y la app funciona desde Android 7 (minSdk 24).
fun esFechaValida(fecha: String): Boolean {
    if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(fecha)) return false
    val formato = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
        isLenient = false
    }
    return try {
        formato.parse(fecha) != null
    } catch (e: java.text.ParseException) {
        false
    }
}
