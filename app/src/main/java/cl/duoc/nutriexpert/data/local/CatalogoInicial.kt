package cl.duoc.nutriexpert.data.local

import androidx.room.withTransaction

// Datos del catálogo que la app carga sola en Room.
// IMPORTANTE: deben ser los MISMOS ids y valores de domain/model/CatalogoEjemplo.kt
// (6 fertilizantes, tomate y lechuga). Con esos números se verifican los casos CP-01 a CP-03.
object CatalogoInicial {

    val fertilizantes = listOf(
        FertilizanteEntity(
            id = "fert-nitrato-calcio", nombre = "Nitrato de calcio",
            n = 16.0, p2o5 = 0.0, k2o = 0.0, cao = 27.0, mgo = 0.0,
            solubilidadGL = 1200.0, grupo = "A"
        )
        // TODO: copiar los otros 5 fertilizantes desde CatalogoEjemplo.kt
    )

    val cultivos = listOf(
        CultivoEntity(
            id = "cultivo-tomate", nombre = "Tomate",
            demandaN = 200.0, demandaP2o5 = 60.0, demandaK2o = 250.0,
            demandaCao = 100.0, demandaMgo = 40.0
        )
        // TODO: copiar la lechuga ("cultivo-lechuga") desde CatalogoEjemplo.kt
    )

    val etapas = listOf(
        EtapaEntity(
            cultivoId = "cultivo-tomate", orden = 3, nombre = "Cuaja y llenado",
            semanas = 8, riegosSemana = 4,
            pctN = 35.0, pctP2o5 = 25.0, pctK2o = 50.0, pctCao = 35.0, pctMgo = 35.0
        )
        // TODO: copiar las demás etapas del tomate y las de la lechuga desde CatalogoEjemplo.kt
    )

    // Guarda (o actualiza) todo el catálogo. Se llama cada vez que se abre la base,
    // así si cambian los datos en el código, el teléfono queda al día.
    // Los cultivos van primero porque las etapas dependen de ellos.
    suspend fun cargar(db: AppDatabase) {
        db.cultivoDao().guardarTodos(cultivos)
        db.fertilizanteDao().guardarTodos(fertilizantes)
        db.etapaDao().guardarTodas(etapas)
    }
}
