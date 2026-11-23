package cl.duoc.nutriexpert.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

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

    // Usuarios: tiempos de espera cortos. Si el servidor está apagado (laboratorio de AWS
    // detenido) o no hay señal, la app no se queda esperando y usa el respaldo local.
    private val clienteUsuarios: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val retrofitUsuarios: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.URL_USUARIOS)
            .client(clienteUsuarios)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val usuarioApi: UsuarioApiService by lazy {
        retrofitUsuarios.create(UsuarioApiService::class.java)
    }
}
