package cl.duoc.nutriexpert.seguridad

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hash seguro de contraseñas con PBKDF2 + sal aleatoria.
 *
 * - La contraseña NUNCA se guarda: en Room solo queda el resultado del hash.
 * - Cada usuario tiene su propia sal, así dos contraseñas iguales dan hashes distintos.
 * - Muchas iteraciones hacen que adivinar contraseñas por fuerza bruta sea lento.
 *
 * Formato guardado: "pbkdf2:<iteraciones>:<sal en hex>:<hash en hex>".
 * Se guardan las iteraciones para poder subirlas en el futuro sin romper cuentas antiguas.
 *
 * Usa PBKDF2WithHmacSHA1 porque está disponible desde Android 7 (minSdk 24);
 * las variantes con SHA-256 recién existen desde Android 8.
 */
object Contrasenas {

    private const val PREFIJO = "pbkdf2"
    private const val SEPARADOR = ":"
    private const val ALGORITMO = "PBKDF2WithHmacSHA1"
    private const val ITERACIONES = 100_000
    private const val LARGO_SAL_BYTES = 16
    private const val LARGO_HASH_BITS = 256

    private val aleatorio = SecureRandom()

    /** Genera el texto que se guarda en la base de datos para esta contraseña. */
    fun generarHash(contrasena: String): String {
        val sal = ByteArray(LARGO_SAL_BYTES).also { aleatorio.nextBytes(it) }
        val hash = derivar(contrasena, sal, ITERACIONES, LARGO_HASH_BITS)
        return listOf(PREFIJO, ITERACIONES.toString(), aHex(sal), aHex(hash))
            .joinToString(SEPARADOR)
    }

    /** true si la contraseña escrita corresponde al hash guardado. */
    fun verificar(contrasena: String, guardado: String): Boolean {
        val partes = guardado.split(SEPARADOR)
        if (partes.size != 4 || partes[0] != PREFIJO) return false
        val iteraciones = partes[1].toIntOrNull() ?: return false
        val sal = deHex(partes[2]) ?: return false
        val esperado = deHex(partes[3]) ?: return false
        if (iteraciones <= 0 || sal.isEmpty() || esperado.isEmpty()) return false

        val calculado = derivar(contrasena, sal, iteraciones, esperado.size * 8)
        // Comparación en tiempo constante: no revela cuántos bytes coinciden
        return MessageDigest.isEqual(esperado, calculado)
    }

    private fun derivar(contrasena: String, sal: ByteArray, iteraciones: Int, bits: Int): ByteArray {
        val spec = PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, bits)
        return try {
            SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    // Hexadecimal a mano: java.util.Base64 no existe en Android 7
    private fun aHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun deHex(texto: String): ByteArray? {
        if (texto.length % 2 != 0) return null
        return try {
            ByteArray(texto.length / 2) { i ->
                texto.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (e: NumberFormatException) {
            null
        }
    }
}
