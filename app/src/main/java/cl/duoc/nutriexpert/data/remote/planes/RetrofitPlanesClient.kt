
package cl.duoc.nutriexpert.data.remote.planes

import cl.duoc.nutriexpert.data.remote.ApiConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitPlanesClient {

    // La dirección está en ApiConfig, junto a las de los otros microservicios
    private const val BASE_URL = ApiConfig.URL_PLANES

    val api: PlanApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlanApiService::class.java)
    }
}
