package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.remote.clima.OpenMeteoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        try {
            val json = OpenMeteoClient.api.obtenerPronostico(
                latitud = latitud,
                longitud = longitud,
                actual = "temperature_2m,weather_code",
                diario = "temperature_2m_max,temperature_2m_min",
                dias = 1
            )

            val actual = json.getAsJsonObject("current")
            val diario = json.getAsJsonObject("daily")

            DatosClima(
                temperatura = actual.get("temperature_2m").asDouble,
                temperaturaMaxima = diario
                    .getAsJsonArray("temperature_2m_max")[0].asDouble,
                temperaturaMinima = diario
                    .getAsJsonArray("temperature_2m_min")[0].asDouble,
                codigoClima = actual.get("weather_code").asInt
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerPronostico7Dias(
        latitud: Double,
        longitud: Double
    ): List<PronosticoDia> = withContext(Dispatchers.IO) {
        try {
            val json = OpenMeteoClient.api.obtenerPronostico(
                latitud = latitud,
                longitud = longitud,
                diario = "temperature_2m_max,temperature_2m_min,weather_code",
                dias = 7
            )

            val diario = json.getAsJsonObject("daily")
            val fechas = diario.getAsJsonArray("time")
            val maximas = diario.getAsJsonArray("temperature_2m_max")
            val minimas = diario.getAsJsonArray("temperature_2m_min")
            val codigos = diario.getAsJsonArray("weather_code")

            (0 until fechas.size()).map { i ->
                PronosticoDia(
                    fecha = fechas[i].asString,
                    temperaturaMaxima = maximas[i].asDouble,
                    temperaturaMinima = minimas[i].asDouble,
                    codigoClima = codigos[i].asInt
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
