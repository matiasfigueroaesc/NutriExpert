package cl.duoc.nutriexpert.data.remote

// DTO = "lo que viaja por internet". Tiene los mismos nombres que el JSON del microservicio
// (contrato del equipo). Es distinto de PredioEntity, que es lo que se guarda en el teléfono.
data class PredioDto(
    val id: Long? = null,        // null al crear: el servidor pone el id
    val usuarioId: Long,
    val nombre: String,
    val comuna: String,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val superficieHa: Double
)
