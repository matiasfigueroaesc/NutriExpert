package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.UsuarioDao
import cl.duoc.nutriexpert.data.local.UsuarioEntity
import cl.duoc.nutriexpert.data.remote.UsuarioApiService
import cl.duoc.nutriexpert.data.remote.UsuarioDto
import cl.duoc.nutriexpert.seguridad.Contrasenas
import cl.duoc.nutriexpert.seguridad.SesionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

// Tests del AuthRepository: el DAO, la sesión y el servidor son "de mentira" (MockK).
// Se prueban los tres caminos: el servidor responde, el servidor rechaza y no hay red.
class AuthRepositoryTest {

    private val dao = mockk<UsuarioDao>(relaxed = true)
    private val sesion = mockk<SesionManager>(relaxed = true)
    private val api = mockk<UsuarioApiService>()

    // El reloj queda fijo para poder probar el bloqueo
    private val repo = AuthRepository(dao, sesion, api, ahora = { 1_000_000L })

    private val hash = Contrasenas.generarHash("clave123")
    private val usuarioLocal = UsuarioEntity(id = 7, nombre = "Ana", correo = "ana@mail.cl", hashContrasena = hash)
    private val usuarioServidor = UsuarioDto(id = 42, nombre = "Ana", email = "ana@mail.cl")

    // Respuesta de error como la manda el microservicio: { "mensaje": "..." }
    private fun <T> respuestaError(codigo: Int, mensaje: String): Response<T> =
        Response.error(codigo, ResponseBody.create(null, "{\"mensaje\":\"$mensaje\"}"))

    // ---------- Registro ----------

    @Test
    fun registrar_servidorOk_guardaConElIdDelServidorEIniciaSesion() = runTest {
        coEvery { api.registrar(any()) } returns Response.success(usuarioServidor)
        val guardado = slot<UsuarioEntity>()
        coEvery { dao.guardarDesdeServidor(capture(guardado)) } returns Unit

        val resultado = repo.registrar(" Ana ", " ANA@mail.cl ", "clave123")

        assertTrue(resultado is ResultadoAuth.Exito)
        assertEquals(42, guardado.captured.id)                     // mismo id que el servidor
        assertEquals("ana@mail.cl", guardado.captured.correo)
        assertTrue(Contrasenas.verificar("clave123", guardado.captured.hashContrasena))
        coVerify { api.registrar(UsuarioDto(nombre = "Ana", email = "ana@mail.cl", password = "clave123")) }
        verify { sesion.iniciar(42) }
    }

    @Test
    fun registrar_correoRepetido_muestraElMensajeDelServidor() = runTest {
        coEvery { api.registrar(any()) } returns respuestaError(400, "Ya existe una cuenta con ese correo")

        val resultado = repo.registrar("Ana", "ana@mail.cl", "clave123")

        assertEquals("Ya existe una cuenta con ese correo", (resultado as ResultadoAuth.Error).mensaje)
        coVerify(exactly = 0) { dao.guardarDesdeServidor(any()) }
        verify(exactly = 0) { sesion.iniciar(any()) }
    }

    @Test
    fun registrar_sinRed_pideInternet() = runTest {
        coEvery { api.registrar(any()) } throws IOException("sin red")

        val resultado = repo.registrar("Ana", "ana@mail.cl", "clave123")

        assertEquals(AuthRepository.SIN_RED_REGISTRO, (resultado as ResultadoAuth.Error).mensaje)
        coVerify(exactly = 0) { dao.guardarDesdeServidor(any()) }
    }

    // ---------- Login con servidor ----------

    @Test
    fun iniciarSesion_servidorOk_guardaCopiaLocalEIniciaSesion() = runTest {
        every { sesion.bloqueadoHasta() } returns 0L
        coEvery { api.login(any()) } returns Response.success(usuarioServidor)

        val resultado = repo.iniciarSesion("ana@mail.cl", "clave123")

        assertTrue(resultado is ResultadoAuth.Exito)
        coVerify { dao.guardarDesdeServidor(match { it.id == 42 }) }
        verify { sesion.reiniciarIntentos() }
        verify { sesion.iniciar(42) }
    }

    @Test
    fun iniciarSesion_servidorRechaza_sumaUnIntento() = runTest {
        every { sesion.bloqueadoHasta() } returns 0L
        every { sesion.intentosFallidos() } returns 1
        coEvery { api.login(any()) } returns respuestaError(401, "Correo o contraseña incorrectos")

        val resultado = repo.iniciarSesion("ana@mail.cl", "otra")

        assertEquals("Correo o contraseña incorrectos", (resultado as ResultadoAuth.Error).mensaje)
        verify { sesion.guardarIntentos(2, 0L) }
        verify(exactly = 0) { sesion.iniciar(any()) }
    }

    @Test
    fun iniciarSesion_quintoError_bloqueaElLogin() = runTest {
        every { sesion.bloqueadoHasta() } returns 0L
        every { sesion.intentosFallidos() } returns 4
        coEvery { api.login(any()) } returns respuestaError(401, "Correo o contraseña incorrectos")

        val resultado = repo.iniciarSesion("ana@mail.cl", "otra")

        assertTrue((resultado as ResultadoAuth.Error).mensaje.startsWith("Demasiados intentos"))
        verify { sesion.guardarIntentos(0, 1_000_000L + 30_000L) }
    }

    @Test
    fun iniciarSesion_bloqueado_noLlamaAlServidor() = runTest {
        every { sesion.bloqueadoHasta() } returns 1_010_000L   // faltan 10 segundos

        val resultado = repo.iniciarSesion("ana@mail.cl", "clave123")

        assertTrue((resultado as ResultadoAuth.Error).mensaje.contains("10 segundos"))
        coVerify(exactly = 0) { api.login(any()) }
    }

    // ---------- Login sin red (respaldo en Room) ----------

    @Test
    fun iniciarSesion_sinRed_conCopiaLocal_entra() = runTest {
        every { sesion.bloqueadoHasta() } returns 0L
        coEvery { api.login(any()) } throws IOException("sin red")
        coEvery { dao.buscarPorCorreo("ana@mail.cl") } returns usuarioLocal

        val resultado = repo.iniciarSesion("ana@mail.cl", "clave123")

        assertTrue(resultado is ResultadoAuth.Exito)
        verify { sesion.iniciar(7) }
    }

    @Test
    fun iniciarSesion_sinRed_contrasenaIncorrecta_sumaUnIntento() = runTest {
        every { sesion.bloqueadoHasta() } returns 0L
        every { sesion.intentosFallidos() } returns 0
        coEvery { api.login(any()) } throws IOException("sin red")
        coEvery { dao.buscarPorCorreo("ana@mail.cl") } returns usuarioLocal

        val resultado = repo.iniciarSesion("ana@mail.cl", "otra")

        assertEquals("Correo o contraseña incorrectos", (resultado as ResultadoAuth.Error).mensaje)
        verify { sesion.guardarIntentos(1, 0L) }
    }

    @Test
    fun iniciarSesion_sinRed_primerIngreso_pideInternet() = runTest {
        every { sesion.bloqueadoHasta() } returns 0L
        coEvery { api.login(any()) } throws IOException("sin red")
        coEvery { dao.buscarPorCorreo(any()) } returns null

        val resultado = repo.iniciarSesion("nuevo@mail.cl", "clave123")

        assertEquals(AuthRepository.SIN_RED_PRIMER_INGRESO, (resultado as ResultadoAuth.Error).mensaje)
        verify(exactly = 0) { sesion.iniciar(any()) }
    }

    @Test
    fun iniciarSesion_servidorConProblemas_usaElRespaldoLocal() = runTest {
        every { sesion.bloqueadoHasta() } returns 0L
        coEvery { api.login(any()) } returns respuestaError(500, "Error interno")
        coEvery { dao.buscarPorCorreo("ana@mail.cl") } returns usuarioLocal

        val resultado = repo.iniciarSesion("ana@mail.cl", "clave123")

        assertTrue(resultado is ResultadoAuth.Exito)
        verify { sesion.iniciar(7) }
    }

    // ---------- Sesión ----------

    @Test
    fun usuarioActual_conSesion_loBuscaEnRoom() = runTest {
        every { sesion.usuarioId() } returns 7
        coEvery { dao.obtenerPorId(7) } returns usuarioLocal

        assertEquals(usuarioLocal, repo.usuarioActual())
    }

    @Test
    fun usuarioActual_sinSesion_esNull() = runTest {
        every { sesion.usuarioId() } returns null

        assertNull(repo.usuarioActual())
    }

    @Test
    fun cerrarSesion_cierraLaSesion() {
        repo.cerrarSesion()
        verify { sesion.cerrar() }
    }
}
