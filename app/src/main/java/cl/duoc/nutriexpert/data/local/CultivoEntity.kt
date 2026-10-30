package cl.duoc.nutriexpert.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "cultivos" del catálogo. Es información fija: la app la carga sola
// (ver CatalogoInicial) y el usuario no la edita.
// Los sectores guardan el id de su cultivo (cultivoId: String).
@Entity(tableName = "cultivos")
data class CultivoEntity(
    @PrimaryKey
    val id: String,             // ej: "cultivo-tomate"
    val nombre: String,         // ej: "Tomate"
    // Lo que necesita el cultivo en la temporada, en kg por hectárea
    val demandaN: Double,
    val demandaP2o5: Double,
    val demandaK2o: Double,
    val demandaCao: Double,
    val demandaMgo: Double
)
