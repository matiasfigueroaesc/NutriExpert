
package cl.duoc.nutriexpert.data.remote.planes

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Body

interface PlanApiService {

    @GET("api/planes")
    suspend fun obtenerPlanes(
        @Query("sectorId") sectorId: Long
    ): List<PlanDto>

    @GET("api/planes/{id}")
    suspend fun obtenerPlanPorId(
        @Path("id") id: Long
    ): PlanDto

    @POST("api/planes")
    suspend fun crearPlan(
        @Body plan: PlanDto
    ): PlanDto

    @PUT("api/planes/{id}")
    suspend fun actualizarPlan(
        @Path("id") id: Long,
        @Body plan: PlanDto
    ): PlanDto

    @DELETE("api/planes/{id}")
    suspend fun eliminarPlan(
        @Path("id") id: Long
    ): Response<Unit>
}
