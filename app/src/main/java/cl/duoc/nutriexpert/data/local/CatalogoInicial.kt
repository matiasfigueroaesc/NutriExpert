package cl.duoc.nutriexpert.data.local

// Datos del catálogo que la app carga sola en Room.
// IMPORTANTE: son los MISMOS ids y valores de domain/model/CatalogoEjemplo.kt
// (6 fertilizantes, tomate y lechuga). Con esos números se verifican los casos CP-01 a CP-03.
// Si cambias un valor aquí, cámbialo también en CatalogoEjemplo (y al revés).
object CatalogoInicial {

    val fertilizantes = listOf(
        FertilizanteEntity(
            id = "fert-map", nombre = "Fosfato monoamónico (MAP)",
            n = 12.0, p2o5 = 60.0, k2o = 0.0, cao = 0.0, mgo = 0.0,
            solubilidadGL = 370.0, grupo = "B"
        ),
        FertilizanteEntity(
            id = "fert-nitrato-calcio", nombre = "Nitrato de calcio",
            n = 16.0, p2o5 = 0.0, k2o = 0.0, cao = 27.0, mgo = 0.0,
            solubilidadGL = 1200.0, grupo = "A"
        ),
        FertilizanteEntity(
            id = "fert-nitrato-potasio", nombre = "Nitrato de potasio",
            n = 13.0, p2o5 = 0.0, k2o = 45.0, cao = 0.0, mgo = 0.0,
            solubilidadGL = 316.0, grupo = "AMBOS"
        ),
        FertilizanteEntity(
            id = "fert-sulfato-mg", nombre = "Sulfato de magnesio 7H₂O",
            n = 0.0, p2o5 = 0.0, k2o = 0.0, cao = 0.0, mgo = 16.0,
            solubilidadGL = 710.0, grupo = "B"
        ),
        FertilizanteEntity(
            id = "fert-urea", nombre = "Urea",
            n = 46.0, p2o5 = 0.0, k2o = 0.0, cao = 0.0, mgo = 0.0,
            solubilidadGL = 1080.0, grupo = "AMBOS"
        ),
        FertilizanteEntity(
            id = "fert-sulfato-k", nombre = "Sulfato de potasio",
            n = 0.0, p2o5 = 0.0, k2o = 50.0, cao = 0.0, mgo = 0.0,
            solubilidadGL = 110.0, grupo = "B"
        )
    )

    val cultivos = listOf(
        CultivoEntity(
            id = "cultivo-tomate", nombre = "Tomate",
            demandaN = 200.0, demandaP2o5 = 60.0, demandaK2o = 250.0,
            demandaCao = 100.0, demandaMgo = 40.0
        ),
        CultivoEntity(
            id = "cultivo-lechuga", nombre = "Lechuga",
            demandaN = 120.0, demandaP2o5 = 40.0, demandaK2o = 150.0,
            demandaCao = 60.0, demandaMgo = 20.0
        )
    )

    // Porcentajes en el orden de mapaNutrientes: N, P₂O₅, K₂O, CaO, MgO
    val etapas = listOf(
        // Tomate
        etapa("cultivo-tomate", 1, "Establecimiento", 6, 3, 20.0, 40.0, 15.0, 15.0, 15.0),
        etapa("cultivo-tomate", 2, "Floración", 4, 3, 30.0, 25.0, 20.0, 25.0, 25.0),
        etapa("cultivo-tomate", 3, "Cuaja y llenado", 8, 4, 35.0, 25.0, 50.0, 35.0, 35.0),
        etapa("cultivo-tomate", 4, "Cosecha", 4, 2, 15.0, 10.0, 15.0, 25.0, 25.0),
        // Lechuga
        etapa("cultivo-lechuga", 1, "Establecimiento", 2, 3, 20.0, 40.0, 20.0, 25.0, 25.0),
        etapa("cultivo-lechuga", 2, "Crecimiento", 4, 3, 50.0, 40.0, 45.0, 45.0, 45.0),
        etapa("cultivo-lechuga", 3, "Formación de cabeza", 2, 3, 30.0, 20.0, 35.0, 30.0, 30.0)
    )

    private fun etapa(
        cultivoId: String, orden: Int, nombre: String, semanas: Int, riegosSemana: Int,
        pctN: Double, pctP2o5: Double, pctK2o: Double, pctCao: Double, pctMgo: Double
    ) = EtapaEntity(
        cultivoId = cultivoId, orden = orden, nombre = nombre,
        semanas = semanas, riegosSemana = riegosSemana,
        pctN = pctN, pctP2o5 = pctP2o5, pctK2o = pctK2o, pctCao = pctCao, pctMgo = pctMgo
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
