package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.remote.clima.OpenMeteoApi
import cl.duoc.nutriexpert.data.remote.clima.OpenMeteoClient
import com.google.gson.JsonParser
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ClimaRepositoryTest {

    private lateinit var api: OpenMeteoApi
    private lateinit var repository: ClimaRepository

    @Before
    fun preparar() {
        mockkObject(OpenMeteoClient)
        api = mockk()
        io.mockk.every { OpenMeteoClient.api } returns api
        repository = ClimaRepository()
    }

    @After
    fun limpiar() {
        unmockkObject(OpenMeteoClient)
    }

    @Test
    fun obtieneTemperaturasCorrectamente() = runBlocking {
        val json = JsonParser.parseString(
            """
            {
              "current": {
                "temperature_2m": 22.5,
                "weather_code": 1
              },
              "daily": {
                "temperature_2m_max": [28.0],
                "temperature_2m_min": [13.0]
              }
            }
            """.trimIndent()
        ).asJsonObject

        coEvery {
            api.obtenerPronostico(
                any(), any(), any(), any(), any(), any()
            )
        } returns json

        val clima = repository.obtenerClima(-33.0, -71.0)

        assertNotNull(clima)
        assertEquals(22.5, clima!!.temperatura, 0.001)
        assertEquals(28.0, clima.temperaturaMaxima, 0.001)
        assertEquals(13.0, clima.temperaturaMinima, 0.001)
        assertEquals(1, clima.codigoClima)
    }

    @Test
    fun devuelveNullCuandoFallaConexion() = runBlocking {
        coEvery {
            api.obtenerPronostico(
                any(), any(), any(), any(), any(), any()
            )
        } throws RuntimeException("Sin conexion")

        val clima = repository.obtenerClima(-33.0, -71.0)

        assertNull(clima)
    }

    @Test
    fun devuelveNullCuandoFaltanDatos() = runBlocking {
        val json = JsonParser.parseString("{}").asJsonObject

        coEvery {
            api.obtenerPronostico(
                any(), any(), any(), any(), any(), any()
            )
        } returns json

        val clima = repository.obtenerClima(-33.0, -71.0)

        assertNull(clima)
    }

    @Test
    fun obtienePronosticoDeSieteDias() = runBlocking {
        val json = JsonParser.parseString(
            """
            {
              "daily": {
                "time": [
                  "2026-11-22",
                  "2026-11-23",
                  "2026-11-24",
                  "2026-11-25",
                  "2026-11-26",
                  "2026-11-27",
                  "2026-11-28"
                ],
                "temperature_2m_max": [25,26,27,28,29,30,31],
                "temperature_2m_min": [10,11,12,13,14,15,16],
                "weather_code": [0,1,2,3,0,1,2]
              }
            }
            """.trimIndent()
        ).asJsonObject

        coEvery {
            api.obtenerPronostico(
                any(), any(), any(), any(), any(), any()
            )
        } returns json

        val dias = repository.obtenerPronostico7Dias(-33.0, -71.0)

        assertEquals(7, dias.size)
        assertEquals("2026-11-22", dias[0].fecha)
        assertEquals(25.0, dias[0].temperaturaMaxima, 0.001)
        assertEquals(16.0, dias[6].temperaturaMinima, 0.001)
        assertEquals(2, dias[6].codigoClima)
    }

    @Test
    fun pronosticoDevuelveListaVaciaSiFallaConexion() = runBlocking {
        coEvery {
            api.obtenerPronostico(
                any(), any(), any(), any(), any(), any()
            )
        } throws RuntimeException("Sin conexion")

        val dias = repository.obtenerPronostico7Dias(-33.0, -71.0)

        assertTrue(dias.isEmpty())
    }

    @Test
    fun pronosticoDevuelveListaVaciaSiFaltanDatos() = runBlocking {
        val json = JsonParser.parseString("{}").asJsonObject

        coEvery {
            api.obtenerPronostico(
                any(), any(), any(), any(), any(), any()
            )
        } returns json

        val dias = repository.obtenerPronostico7Dias(-33.0, -71.0)

        assertTrue(dias.isEmpty())
    }
}
