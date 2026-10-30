package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.CultivoDao
import cl.duoc.nutriexpert.data.local.CultivoEntity
import cl.duoc.nutriexpert.data.local.EtapaDao
import cl.duoc.nutriexpert.data.local.EtapaEntity
import cl.duoc.nutriexpert.data.local.FertilizanteDao
import cl.duoc.nutriexpert.data.local.FertilizanteEntity
import kotlinx.coroutines.flow.Flow

// Repositorio del catálogo (solo lectura): cultivos, fertilizantes y etapas.
// Uso: CatalogoRepository(db.cultivoDao(), db.fertilizanteDao(), db.etapaDao())
class CatalogoRepository(
    private val cultivoDao: CultivoDao,
    private val fertilizanteDao: FertilizanteDao,
    private val etapaDao: EtapaDao
) {

    // Para el desplegable de cultivos (formulario de sector y paso 1 del plan)
    fun cultivos(): Flow<List<CultivoEntity>> = cultivoDao.obtenerTodos()

    suspend fun cultivo(id: String): CultivoEntity? = cultivoDao.obtenerPorId(id)

    fun fertilizantes(): Flow<List<FertilizanteEntity>> = fertilizanteDao.obtenerTodos()

    fun etapas(cultivoId: String): Flow<List<EtapaEntity>> = etapaDao.obtenerDeCultivo(cultivoId)

    // TODO: cuando esté el código del plan, agregar aquí las funciones que devuelven
    // FertilizanteInfo y CultivoEjemplo (los modelos del motor) para el PlanViewModel.
}
