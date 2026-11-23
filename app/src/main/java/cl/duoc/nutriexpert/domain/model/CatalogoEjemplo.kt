package cl.duoc.nutriexpert.domain.model

/**
 * Catálogo inicial de ejemplo (etapa 1). Datos provisorios hasta recibir el Excel del
 * cliente (puntos por definir 4 y 6): se cargan en Room la primera vez que abre la app.
 * Los fertilizantes y la curva de tomate reproducen los casos CP-01 a CP-03.
 */
object CatalogoEjemplo {

    val MAP = FertilizanteInfo(
        id = "fert-map", nombre = "Fosfato monoamónico (MAP)",
        contenido = mapaNutrientes(n = 12.0, p2o5 = 60.0),
        solubilidadGL = 370.0, grupo = GrupoEstanque.B
    )
    val NITRATO_CALCIO = FertilizanteInfo(
        id = "fert-nitrato-calcio", nombre = "Nitrato de calcio",
        contenido = mapaNutrientes(n = 16.0, cao = 27.0),
        solubilidadGL = 1200.0, grupo = GrupoEstanque.A
    )
    val NITRATO_POTASIO = FertilizanteInfo(
        id = "fert-nitrato-potasio", nombre = "Nitrato de potasio",
        contenido = mapaNutrientes(n = 13.0, k2o = 45.0),
        solubilidadGL = 316.0, grupo = GrupoEstanque.AMBOS
    )
    val SULFATO_MAGNESIO = FertilizanteInfo(
        id = "fert-sulfato-mg", nombre = "Sulfato de magnesio 7H₂O",
        contenido = mapaNutrientes(mgo = 16.0),
        solubilidadGL = 710.0, grupo = GrupoEstanque.B
    )
    val UREA = FertilizanteInfo(
        id = "fert-urea", nombre = "Urea",
        contenido = mapaNutrientes(n = 46.0),
        solubilidadGL = 1080.0, grupo = GrupoEstanque.AMBOS
    )
    val SULFATO_POTASIO = FertilizanteInfo(
        id = "fert-sulfato-k", nombre = "Sulfato de potasio",
        contenido = mapaNutrientes(k2o = 50.0),
        solubilidadGL = 110.0, grupo = GrupoEstanque.B
    )

    val FERTILIZANTES = listOf(MAP, NITRATO_CALCIO, NITRATO_POTASIO, SULFATO_MAGNESIO, UREA, SULFATO_POTASIO)

    /** Fuentes preferidas por defecto en el paso 2 (las 5 del CP-01). */
    val PREFERIDOS_POR_DEFECTO = setOf(MAP.id, NITRATO_CALCIO.id, NITRATO_POTASIO.id, SULFATO_MAGNESIO.id, UREA.id)

    data class CultivoEjemplo(
        val id: String,
        val nombre: String,
        /** Demanda de ejemplo en kg/ha; su origen definitivo está por definir (punto 3). */
        val demanda: Map<Nutriente, Double>,
        val etapas: List<EtapaInfo>
    )

    /** Curva de tomate: K₂O 15/20/50/15 (CP-02); E3 de N, CaO y MgO 35 % y P₂O₅ 25 % (CP-03). */
    val TOMATE = CultivoEjemplo(
        id = "cultivo-tomate",
        nombre = "Tomate",
        demanda = mapaNutrientes(n = 200.0, p2o5 = 60.0, k2o = 250.0, cao = 100.0, mgo = 40.0),
        etapas = listOf(
            EtapaInfo(1, "Establecimiento", 6, 3, mapaNutrientes(20.0, 40.0, 15.0, 15.0, 15.0)),
            EtapaInfo(2, "Floración", 4, 3, mapaNutrientes(30.0, 25.0, 20.0, 25.0, 25.0)),
            EtapaInfo(3, "Cuaja y llenado", 8, 4, mapaNutrientes(35.0, 25.0, 50.0, 35.0, 35.0)),
            EtapaInfo(4, "Cosecha", 4, 2, mapaNutrientes(15.0, 10.0, 15.0, 25.0, 25.0))
        )
    )

    val LECHUGA = CultivoEjemplo(
        id = "cultivo-lechuga",
        nombre = "Lechuga",
        demanda = mapaNutrientes(n = 120.0, p2o5 = 40.0, k2o = 150.0, cao = 60.0, mgo = 20.0),
        etapas = listOf(
            EtapaInfo(1, "Establecimiento", 2, 3, mapaNutrientes(20.0, 40.0, 20.0, 25.0, 25.0)),
            EtapaInfo(2, "Crecimiento", 4, 3, mapaNutrientes(50.0, 40.0, 45.0, 45.0, 45.0)),
            EtapaInfo(3, "Formación de cabeza", 2, 3, mapaNutrientes(30.0, 20.0, 35.0, 30.0, 30.0))
        )
    )

    val CULTIVOS = listOf(TOMATE, LECHUGA)
}
