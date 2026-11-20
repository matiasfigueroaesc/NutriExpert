
package cl.duoc.nutriexpert.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class DatosClima(
    val temperatura: Double,
    val temperaturaMaxima: Double,
    val temperaturaMinima: Double,
    val codigoClima: Int
)


data class PronosticoDia(
    val fecha: String,
    val temperaturaMaxima: Double,
    val temperaturaMinima: Double,
    val codigoClima: Int
)
class ClimaRepository {

    suspend fun obtenerClima(
        latitud: Double,
        longitud: Double
    ): DatosClima? = withContext(Dispatchers.IO) {

        var conexion: HttpURLConnection? = null

        try {
            val url = URL(
                "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=${String.format(Locale.US, "%.6f", latitud)}" +
                        "&longitude=${String.format(Locale.US, "%.6f", longitud)}" +
                        "&current=temperature_2m,weather_code" +
                        "&daily=temperature_2m_max,temperature_2m_min" +
                        "&timezone=auto" +
                        "&forecast_days=1"
            )

            conexion = url.openConnection() as HttpURLConnection

            conexion.requestMethod = "GET"
            conexion.connectTimeout = 10000
            conexion.readTimeout = 10000

            if (conexion.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext null
            }

            val respuesta = conexion.inputStream
                .bufferedReader()
                .use { it.readText() }

            val json = JSONObject(respuesta)

            val actual = json.getJSONObject("current")
            val diario = json.getJSONObject("daily")

            DatosClima(
                temperatura = actual.getDouble("temperature_2m"),
                temperaturaMaxima = diario
                    .getJSONArray("temperature_2m_max")
                    .getDouble(0),
                temperaturaMinima = diario
                    .getJSONArray("temperature_2m_min")
                    .getDouble(0),
                codigoClima = actual.getInt("weather_code")
            )

        } catch (e: Exception) {
            null
        } finally {
            conexion?.disconnect()
        }
    }

    suspend fun obtenerPronostico7Dias(
        latitud: Double,
        longitud: Double
    ): List<PronosticoDia> = withContext(Dispatchers.IO) {
        var conexion: HttpURLConnection? = null

        try {
            val url = URL(
                "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=${String.format(Locale.US, "%.6f", latitud)}" +
                    "&longitude=${String.format(Locale.US, "%.6f", longitud)}" +
                    "&daily=temperature_2m_max,temperature_2m_min,weather_code" +
                    "&timezone=auto" +
                    "&forecast_days=7"
            )

            conexion = url.openConnection() as HttpURLConnection
            conexion.requestMethod = "GET"
            conexion.connectTimeout = 10000
            conexion.readTimeout = 10000

            if (conexion.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext emptyList()
            }

            val respuesta = conexion.inputStream.bufferedReader()
                .use { it.readText() }

            val diario = JSONObject(respuesta).getJSONObject("daily")
            val fechas = diario.getJSONArray("time")
            val maximas = diario.getJSONArray("temperature_2m_max")
            val minimas = diario.getJSONArray("temperature_2m_min")
            val codigos = diario.getJSONArray("weather_code")

            (0 until fechas.length()).map { i ->
                PronosticoDia(
                    fecha = fechas.getString(i),
                    temperaturaMaxima = maximas.getDouble(i),
                    temperaturaMinima = minimas.getDouble(i),
                    codigoClima = codigos.getInt(i)
                )
            }
        } catch (e: Exception) {
            emptyList()
        } finally {
            conexion?.disconnect()
        }
    }
}

