package cl.duoc.nutriexpert.nativo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Recurso nativo 1: ubicación GPS.
 *
 * Usa el LocationManager de Android (no necesita Google Play Services ni
 * dependencias nuevas en Gradle) y funciona sin internet usando el GPS.
 */
object UbicacionHelper {

    val PERMISOS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /** Tiempo máximo esperando una lectura nueva por proveedor. */
    private const val TIMEOUT_MS = 15_000L

    /** Una ubicación guardada más nueva que esto se usa directo. */
    private const val MAX_ANTIGUEDAD_MS = 2 * 60 * 1000L

    fun tienePermiso(context: Context): Boolean =
        PERMISOS.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    fun ubicacionActivada(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(lm)
    }

    /**
     * Devuelve la ubicación actual del teléfono, o null si no hay permiso,
     * la ubicación está apagada o no se logró obtener a tiempo.
     */
    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacion(context: Context): Location? {
        if (!tienePermiso(context)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val proveedores = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
        if (proveedores.isEmpty()) return null

        // 1) Si hay una lectura reciente guardada, la usamos (es instantáneo).
        val ultima = proveedores
            .mapNotNull { p ->
                try { lm.getLastKnownLocation(p) } catch (e: SecurityException) { null }
            }
            .maxByOrNull { it.time }
        if (ultima != null && System.currentTimeMillis() - ultima.time < MAX_ANTIGUEDAD_MS) {
            return ultima
        }

        // 2) Si no, pedimos una lectura nueva (primero GPS, después red).
        for (proveedor in proveedores) {
            val actual = withTimeoutOrNull(TIMEOUT_MS) { pedirUbicacionActual(context, lm, proveedor) }
            if (actual != null) return actual
        }

        // 3) Último recurso: la lectura guardada aunque sea antigua.
        return ultima
    }

    @SuppressLint("MissingPermission")
    private suspend fun pedirUbicacionActual(
        context: Context,
        lm: LocationManager,
        proveedor: String
    ): Location? = suspendCancellableCoroutine { cont ->
        val signal = CancellationSignal()
        cont.invokeOnCancellation { signal.cancel() }
        try {
            LocationManagerCompat.getCurrentLocation(
                lm,
                proveedor,
                signal,
                ContextCompat.getMainExecutor(context)
            ) { location ->
                if (cont.isActive) cont.resume(location)
            }
        } catch (e: SecurityException) {
            // Pasa si solo se dio permiso de ubicación aproximada y pedimos GPS.
            if (cont.isActive) cont.resume(null)
        } catch (e: IllegalArgumentException) {
            if (cont.isActive) cont.resume(null)
        }
    }
}
