package cl.duoc.nutriexpert.data.remote

import com.google.gson.Gson
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Los 5 pedidos que la app le puede hacer al microservicio de usuarios (puerto 8080).
// Solo se escribe la "forma" del pedido; Retrofit arma el código que lo envía.
// Response<...> deja revisar el código de respuesta (200, 201, 400, 401, 404...).
interface UsuarioApiService {

    @POST("api/usuarios/registro")
    suspend fun registrar(@Body datos: UsuarioDto): Response<UsuarioDto>

    @POST("api/usuarios/login")
    suspend fun login(@Body datos: UsuarioDto): Response<UsuarioDto>

    @GET("api/usuarios/{id}")
    suspend fun buscar(@Path("id") id: Long): Response<UsuarioDto>

    // Solo cambia el nombre
    @PUT("api/usuarios/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body datos: UsuarioDto): Response<UsuarioDto>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}

// Lee el "mensaje" que manda el servidor cuando responde con error (400, 401...).
// Devuelve null si no viene o no se puede leer.
fun Response<*>.mensajeDeError(): String? = try {
    errorBody()?.string()?.let { Gson().fromJson(it, MensajeDto::class.java)?.mensaje }
} catch (e: Exception) {
    null
}
