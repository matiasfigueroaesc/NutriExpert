package cl.duoc.nutriexpert.data.remote.planes

data class PlanDto(
    val id: Long? = null,
    val sectorId: Long,
    val nombreTemporada: String,
    val fechaInicio: String? = null,
    val fechaTermino: String? = null
)