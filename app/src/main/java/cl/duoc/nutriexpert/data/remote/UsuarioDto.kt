package cl.duoc.nutriexpert.data.remote

// DTO = "lo que viaja por internet" hacia el microservicio usuarios.
// Mismos nombres que el JSON del contrato. Todo es opcional porque cada pedido usa
// solo algunos campos: el registro manda nombre, email y password; el login, email y
// password; cambiar el nombre, solo nombre. Gson no envía los campos que van en null.
// La respuesta del servidor trae id, nombre y email, nunca la contraseña.
data class UsuarioDto(
    val id: Long? = null,
    val nombre: String? = null,
    val email: String? = null,
    val password: String? = null
)

// Cuerpo de las respuestas de error del microservicio: { "mensaje": "..." }
data class MensajeDto(
    val mensaje: String? = null
)
