
package cl.duoc.nutriexpert.data.remote.planes

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitPlanesClient {

    private const val BASE_URL = "http://127.0.0.1:8082/"

    val api: PlanApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlanApiService::class.java)
    }
}
