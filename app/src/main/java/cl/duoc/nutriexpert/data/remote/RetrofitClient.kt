package cl.duoc.nutriexpert.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Arma Retrofit una sola vez para toda la app.
// "by lazy" = se crea la primera vez que alguien lo usa, no antes.
object RetrofitClient {

    private val retrofitPredios: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.URL_PREDIOS)
            .addConverterFactory(GsonConverterFactory.create()) // JSON <-> objetos Kotlin
            .build()
    }

    val predioApi: PredioApiService by lazy {
        retrofitPredios.create(PredioApiService::class.java)
    }

    val sectorApi: SectorApiService by lazy {
        retrofitPredios.create(SectorApiService::class.java)
    }
}
