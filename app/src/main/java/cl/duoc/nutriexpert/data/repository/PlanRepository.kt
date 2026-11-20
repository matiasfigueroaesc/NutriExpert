
package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.PlanDao
import cl.duoc.nutriexpert.data.local.PlanEntity
import cl.duoc.nutriexpert.data.remote.planes.PlanDto
import cl.duoc.nutriexpert.data.remote.planes.PlanApiService
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class PlanRepository(
    private val planDao: PlanDao,
    private val api: PlanApiService
) {

    fun obtenerPlanes(sectorId: Int): Flow<List<PlanEntity>> {
        return planDao.obtenerPorSector(sectorId)
    }

    suspend fun guardarPlan(
        sectorId: Int,
        nombreTemporada: String
    ): Long {
        val plan = PlanEntity(
            sectorId = sectorId,
            nombreTemporada = nombreTemporada,
            sincronizado = false
        )

        val idLocal = planDao.insertar(plan)

        sincronizarPendientes()

        return idLocal
    }

    suspend fun sincronizarPendientes() {
        val pendientes = planDao.obtenerPendientes()

        for (plan in pendientes) {
            try {
                val dto = PlanDto(
                    id = plan.remoteId,
                    sectorId = plan.sectorId.toLong(),
                    nombreTemporada = plan.nombreTemporada
                )

                val respuesta = if (plan.remoteId == null) {
                    api.crearPlan(dto)
                } else {
                    api.actualizarPlan(plan.remoteId, dto)
                }

                planDao.actualizar(
                    plan.copy(
                        remoteId = respuesta.id,
                        sincronizado = true
                    )
                )
            } catch (e: IOException) {
                // Sin conexión: el plan permanece pendiente en Room.
                return
            }
        }
    }
}
