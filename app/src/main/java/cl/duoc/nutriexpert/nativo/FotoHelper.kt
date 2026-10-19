package cl.duoc.nutriexpert.nativo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Recurso nativo 2: cámara y galería para la foto del predio.
 *
 * Las fotos se guardan dentro de la app (carpeta "fotos_predios"), así siguen
 * disponibles sin internet y aunque el usuario borre la original de la galería.
 * En Room se guarda solo la ruta como texto (campo fotoUri).
 */
object FotoHelper {

    const val PERMISO_CAMARA = Manifest.permission.CAMERA
    private const val CARPETA = "fotos_predios"

    fun tienePermisoCamara(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, PERMISO_CAMARA) == PackageManager.PERMISSION_GRANTED

    private fun autoridad(context: Context) = "${context.packageName}.fileprovider"

    /** Crea un archivo vacío para que la cámara escriba la foto ahí. */
    fun crearUriParaFoto(context: Context): Uri {
        val carpeta = File(context.filesDir, CARPETA).apply { mkdirs() }
        val archivo = File(carpeta, "predio_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, autoridad(context), archivo)
    }

    /** Copia una imagen elegida de la galería a la carpeta de la app. */
    suspend fun copiarDesdeGaleria(context: Context, origen: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val carpeta = File(context.filesDir, CARPETA).apply { mkdirs() }
            val destino = File(carpeta, "predio_${System.currentTimeMillis()}.jpg")
            val copiado = context.contentResolver.openInputStream(origen)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
                true
            } ?: false
            if (copiado) FileProvider.getUriForFile(context, autoridad(context), destino) else null
        } catch (e: Exception) {
            null
        }
    }

    /** Borra una foto de la app (por ejemplo si se canceló la cámara o se cambió la foto). */
    fun borrar(context: Context, uriTexto: String?) {
        if (uriTexto.isNullOrBlank()) return
        runCatching { context.contentResolver.delete(Uri.parse(uriTexto), null, null) }
    }

    /**
     * Carga la imagen reducida para la vista previa, corrigiendo la rotación
     * (muchas cámaras guardan la foto "acostada").
     */
    suspend fun cargarVistaPrevia(context: Context, uriTexto: String, maxLado: Int = 1024): ImageBitmap? =
        withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriTexto)
                val resolver = context.contentResolver

                val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
                if (limites.outWidth <= 0 || limites.outHeight <= 0) return@withContext null

                var escala = 1
                while (limites.outWidth / (escala * 2) >= maxLado || limites.outHeight / (escala * 2) >= maxLado) {
                    escala *= 2
                }
                val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
                val bitmap = resolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opciones)
                } ?: return@withContext null

                val grados = resolver.openInputStream(uri)?.use {
                    when (ExifInterface(it).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                    )) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                } ?: 0f

                rotar(bitmap, grados).asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }

    private fun rotar(bitmap: Bitmap, grados: Float): Bitmap {
        if (grados == 0f) return bitmap
        val matriz = Matrix().apply { postRotate(grados) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
    }
}
