package cl.duoc.nutriexpert.utils

// Formato de correo: texto@dominio.extension (ej: juan.perez@duocuc.cl)
private val REGEX_EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun esEmailValido(email: String): Boolean {
    return REGEX_EMAIL.matches(email.trim())
}
