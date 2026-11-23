package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.PlanDao
import cl.duoc.nutriexpert.data.local.PlanEntity
import cl.duoc.nutriexpert.data.remote.planes.PlanApiService
import cl.duoc.nutriexpert.data.remote.planes.PlanDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class PlanRepository(
    private val planDao: PlanDao,
    private val api: PlanApiService
) {
    fun obtenerPlanes(sectorId: Int): Flow<List<PlanEntity>> =
        planDao.obtenerPorSector(sectorId)

    suspend fun guardarPlan(sectorId: Int, nombreTemporada: String, fechaInicio: String, fechaTermino: String, detalleJson: String? = null): Long {
        require(nombreTemporada.isNotBlank())

        val idLocal = planDao.insertar(
            PlanEntity(
                sectorId = sectorId,
                nombreTemporada = nombreTemporada.trim(),
                fechaInicio = fechaInicio,
                fechaTermino = fechaTermino,
                sincronizado = false,
                detalleJson = detalleJson
            )
        )
        sincronizarPendientes()
        return idLocal
    }

    suspend fun editarPlan(id: Int, nuevoNombre: String) {
        require(nuevoNombre.isNotBlank())
        val plan = planDao.obtenerPorId(id) ?: return

        planDao.actualizar(
            plan.copy(
                nombreTemporada = nuevoNombre.trim(),
                sincronizado = false
            )
        )
        sincronizarPendientes()
    }

    suspend fun eliminarPlan(id: Int) {
        val plan = planDao.obtenerPorId(id) ?: return

        if (plan.remoteId == null) {
            planDao.eliminar(plan)
        } else {
            planDao.actualizar(
                plan.copy(
                    pendienteEliminar = true,
                    sincronizado = false
                )
            )
            sincronizarPendientes()
        }
    }

    suspend fun sincronizarPendientes() {
        for (plan in planDao.obtenerPendientes()) {
            try {
                if (plan.pendienteEliminar) {
                    plan.remoteId?.let { api.eliminarPlan(it) }
                    planDao.eliminar(plan)
                } else {
                    val dto = PlanDto(
                        id = plan.remoteId,
                        sectorId = plan.sectorId.toLong(),
                        nombreTemporada = plan.nombreTemporada,
                        fechaInicio = plan.fechaInicio,
                        fechaTermino = plan.fechaTermino
                    )

                    val respuesta = if (plan.remoteId == null) {
                        api.crearPlan(dto)
                    } else {
                        api.actualizarPlan(plan.remoteId, dto)
                    }

                    planDao.actualizar(
                        plan.copy(
                            remoteId = respuesta.id ?: plan.remoteId,
                            sincronizado = true
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // El cambio queda pendiente para sincronizar.
                continue
            }
        }
    }
}


