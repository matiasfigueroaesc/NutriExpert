package cl.duoc.nutriexpert.data.remote.clima

import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.JsonObject

interface OpenMeteoApi {

    @GET("v1/forecast")
    suspend fun obtenerPronostico(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") actual: String? = null,
        @Query("daily") diario: String,
        @Query("timezone") zonaHoraria: String = "auto",
        @Query("forecast_days") dias: Int
    ): JsonObject
}
