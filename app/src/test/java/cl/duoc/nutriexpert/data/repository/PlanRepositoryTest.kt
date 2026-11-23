package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.PlanDao
import cl.duoc.nutriexpert.data.local.PlanEntity
import cl.duoc.nutriexpert.data.remote.planes.PlanApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PlanRepositoryTest {

    private val dao = mockk<PlanDao>(relaxed = true)
    private val api = mockk<PlanApiService>()
    private val repository = PlanRepository(dao, api)

    @Test
    fun rechazaNombreVacio() = runBlocking {
        var rechazado = false

        try {
            repository.guardarPlan(1, "   ", "2026-11-22", "2027-02-22")
        } catch (e: IllegalArgumentException) {
            rechazado = true
        }

        assertEquals(true, rechazado)
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    @Test
    fun guardaPlanConDetalleJson() = runBlocking {
        val detalle = """{"cultivo":"Tomate"}"""

        coEvery { dao.insertar(any()) } returns 10L
        coEvery { dao.obtenerPendientes() } returns emptyList()

        val id = repository.guardarPlan(
            3, " Temporada verano ", "2026-11-22", "2027-02-22", detalle
        )

        assertEquals(10L, id)

        coVerify(exactly = 1) {
            dao.insertar(match {
                it.sectorId == 3 &&
                it.nombreTemporada == "Temporada verano" &&
                it.detalleJson == detalle &&
                !it.sincronizado
            })
        }
    }

    @Test
    fun mantienePlanPendienteSiFallaApi() = runBlocking {
        val plan = PlanEntity(
            id = 10,
            sectorId = 3,
            nombreTemporada = "Verano",
            sincronizado = false
        )

        coEvery { dao.obtenerPendientes() } returns listOf(plan)
        coEvery { api.crearPlan(any()) } throws RuntimeException("Sin internet")

        repository.sincronizarPendientes()

        assertFalse(plan.sincronizado)
        coVerify(exactly = 0) { dao.actualizar(any()) }
        coVerify(exactly = 0) { dao.eliminar(any()) }
    }

    @Test
    fun rechazaEditarConNombreVacio() = runBlocking {
        var rechazado = false
        try {
            repository.editarPlan(10, "   ")
        } catch (e: IllegalArgumentException) {
            rechazado = true
        }
        assertEquals(true, rechazado)
        coVerify(exactly = 0) { dao.actualizar(any()) }
    }

    @Test
    fun editaNombreYMarcaPendiente() = runBlocking {
        val plan = PlanEntity(
            id = 10,
            sectorId = 3,
            nombreTemporada = "Anterior",
            sincronizado = true
        )
        coEvery { dao.obtenerPorId(10) } returns plan
        coEvery { dao.obtenerPendientes() } returns emptyList()

        repository.editarPlan(10, " Nueva temporada ")

        coVerify(exactly = 1) {
            dao.actualizar(match {
                it.id == 10 &&
                it.nombreTemporada == "Nueva temporada" &&
                !it.sincronizado
            })
        }
    }

    @Test
    fun eliminaDirectamentePlanLocal() = runBlocking {
        val plan = PlanEntity(
            id = 10,
            sectorId = 3,
            nombreTemporada = "Verano"
        )
        coEvery { dao.obtenerPorId(10) } returns plan

        repository.eliminarPlan(10)

        coVerify(exactly = 1) { dao.eliminar(plan) }
    }

    @Test
    fun marcaParaEliminarPlanRemoto() = runBlocking {
        val plan = PlanEntity(
            id = 10,
            sectorId = 3,
            nombreTemporada = "Verano",
            remoteId = 50L,
            sincronizado = true
        )
        coEvery { dao.obtenerPorId(10) } returns plan
        coEvery { dao.obtenerPendientes() } returns emptyList()

        repository.eliminarPlan(10)

        coVerify(exactly = 1) {
            dao.actualizar(match {
                it.id == 10 &&
                it.pendienteEliminar &&
                !it.sincronizado
            })
        }
    }
}

