package cl.duoc.nutriexpert.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Los 5 pedidos que la app le puede hacer al microservicio de predios, para los sectores.
// Solo se escribe la "forma" del pedido; Retrofit arma el código que lo envía.
// Response<...> deja revisar el código de respuesta (201, 404, etc.).
interface SectorApiService {

    @GET("api/sectores")
    suspend fun listar(@Query("predioId") predioId: Long): Response<List<SectorDto>>

    @GET("api/sectores/{id}")
    suspend fun buscar(@Path("id") id: Long): Response<SectorDto>

    @POST("api/sectores")
    suspend fun crear(@Body sector: SectorDto): Response<SectorDto>

    @PUT("api/sectores/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body sector: SectorDto): Response<SectorDto>

    @DELETE("api/sectores/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}
