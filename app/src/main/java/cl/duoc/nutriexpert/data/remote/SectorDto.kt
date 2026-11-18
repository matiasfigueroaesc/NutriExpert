package cl.duoc.nutriexpert.data.remote

// Sector tal como viaja en el JSON. predioId es el id del predio EN EL SERVIDOR.
data class SectorDto(
    val id: Long? = null,
    val predioId: Long,
    val nombre: String,
    val cultivo: String,
    val superficieHa: Double
)
