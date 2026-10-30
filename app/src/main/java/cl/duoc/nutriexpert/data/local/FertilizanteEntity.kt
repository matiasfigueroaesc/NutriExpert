package cl.duoc.nutriexpert.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "fertilizantes" del catálogo (información fija, la carga CatalogoInicial).
@Entity(tableName = "fertilizantes")
data class FertilizanteEntity(
    @PrimaryKey
    val id: String,             // ej: "fert-nitrato-calcio"
    val nombre: String,         // ej: "Nitrato de calcio"
    // Porcentaje de cada nutriente en el fertilizante
    val n: Double,
    val p2o5: Double,
    val k2o: Double,
    val cao: Double,
    val mgo: Double,
    val solubilidadGL: Double,  // gramos que se disuelven por litro
    val grupo: String           // estanque: "A", "B" o "AMBOS"
)
