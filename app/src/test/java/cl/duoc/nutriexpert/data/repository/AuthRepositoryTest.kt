package cl.duoc.nutriexpert.data.repository

import cl.duoc.nutriexpert.data.local.UsuarioDao
import cl.duoc.nutriexpert.data.local.UsuarioEntity
import cl.duoc.nutriexpert.seguridad.Contrasenas
import cl.duoc.nutriexpert.seguridad.SesionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Tests del AuthRepository: el DAO y la sesión son "de mentira" (MockK)
class AuthRepositoryTest {

    private val dao = mockk<UsuarioDao>(relaxed = true)
    private val sesion = mockk<SesionManager>(relaxed = true)

    // El reloj queda fijo para poder probar el bloqueo
    private val repo = AuthRepository(dao, sesion, ahora = { 1_000_000L })

    private val hash = Contrasenas.generarHash("clave123")
    private val usuario = UsuarioEntity(id = 7, nombre = "Ana", correo = "ana@mail.cl", hashContrasena = hash)

    @Test
    fun registrar_correoNuevo_creaLaCuentaEIniciaSesion() = runTest {
        coEvery { dao.buscarPorCorreo("ana@mail.cl") } returns null
        coEvery { dao.insertar(any()) } returns 7L

        val resultado = repo.registrar(" Ana ", " ANA@mail.cl ", "clave123")

        assertTrue(resultado is ResultadoAuth.Exito)
        val creado = (resultado as ResultadoAuth.Exito).usuario
        assertEquals(7, creado.id)
        assertEquals("Ana", creado.nombre)
        assertEquals("ana@mail.cl", creado.correo)
        verify { sesion.iniciar(7) }
    }

    @Test
    fun registrar_correoRepetido_devuelveError() = runTest {
        coEvery { dao.buscarPorCorreo("ana@mail.cl") } returns usuario

        val resultado = repo.registrar("Ana", "ana@mail.cl", "clave123")

        assertEquals("Ya existe una cuenta con ese correo", (resultado as ResultadoAuth.Error).mensaje)
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    @Test
    fun iniciarSesion_datosCorrectos_iniciaSesion() = runTest {
        every { sesion.bloqueadoHasta() } returns 0L
        coEvery { dao.buscarPorCorreo("ana@mail.cl") } returns usuario

        val resultado = repo.iniciarSesion("ana@mail.cl", "clave123")

        assertTrue(resultado is ResultadoAuth.Exito)
        verify { sesion.reiniciarIntentos() }
        verify { sesion.iniciar(7) }
    }

    @Test
    fun iniciarSesion_contrasenaIncorrecta_sumaUnIntento() = runTest {
        every { sesion.bloqueadoHasta() } returns 0L
        every { sesion.intentosFallidos() } returns 1
        coEvery { dao.buscarPorCorreo("ana@mail.cl") } returns usuario

        val resultado = repo.iniciarSesion("ana@mail.cl", "otra")

        assertEquals("Correo o contraseña incorrectos", (resultado as ResultadoAuth.Error).mensaje)
        verify { sesion.guardarIntentos(2, 0L) }
    }

    @Test
    fun iniciarSesion_correoQueNoExiste_devuelveError() = runTest {
        every { sesion.bloqueadoHasta() } returns 0L
        every { sesion.intentosFallidos() } returns 0
        coEvery { dao.buscarPorCorreo(any()) } returns null

        val resultado = repo.iniciarSesion("nadie@mail.cl", "clave123")

        assertTrue(resultado is ResultadoAuth.Error)
        verify(exactly = 0) { sesion.iniciar(any()) }
    }

    @Test
    fun iniciarSesion_quintoError_bloqueaElLogin() = runTest {
        every { sesion.bloqueadoHasta() } returns 0L
        every { sesion.intentosFallidos() } returns 4
        coEvery { dao.buscarPorCorreo("ana@mail.cl") } returns usuario

        val resultado = repo.iniciarSesion("ana@mail.cl", "otra")

        assertTrue((resultado as ResultadoAuth.Error).mensaje.startsWith("Demasiados intentos"))
        verify { sesion.guardarIntentos(0, 1_000_000L + 30_000L) }
    }

    @Test
    fun iniciarSesion_bloqueado_noRevisaLaContrasena() = runTest {
        every { sesion.bloqueadoHasta() } returns 1_010_000L   // faltan 10 segundos

        val resultado = repo.iniciarSesion("ana@mail.cl", "clave123")

        assertTrue((resultado as ResultadoAuth.Error).mensaje.contains("10 segundos"))
        coVerify(exactly = 0) { dao.buscarPorCorreo(any()) }
    }

    @Test
    fun usuarioActual_conSesion_loBuscaEnRoom() = runTest {
        every { sesion.usuarioId() } returns 7
        coEvery { dao.obtenerPorId(7) } returns usuario

        assertEquals(usuario, repo.usuarioActual())
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
