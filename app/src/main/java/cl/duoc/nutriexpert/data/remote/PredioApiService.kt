package cl.duoc.nutriexpert.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Los 5 pedidos que la app le puede hacer al microservicio de predios.
// Solo se escribe la "forma" del pedido; Retrofit arma el código que lo envía.
// Response<...> deja revisar el código de respuesta (201, 404, etc.).
interface PredioApiService {

    @GET("api/predios")
    suspend fun listar(@Query("usuarioId") usuarioId: Long): Response<List<PredioDto>>

    @GET("api/predios/{id}")
    suspend fun buscar(@Path("id") id: Long): Response<PredioDto>

    @POST("api/predios")
    suspend fun crear(@Body predio: PredioDto): Response<PredioDto>

    @PUT("api/predios/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body predio: PredioDto): Response<PredioDto>

    @DELETE("api/predios/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}
