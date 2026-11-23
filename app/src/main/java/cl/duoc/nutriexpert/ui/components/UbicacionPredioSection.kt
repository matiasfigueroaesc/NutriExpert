package cl.duoc.nutriexpert.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.nutriexpert.nativo.UbicacionHelper
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Sección "Ubicación del predio" para el formulario.
 *
 * La pantalla no guarda nada por su cuenta: avisa con [onUbicacionObtenida]
 * y el ViewModel decide qué hacer (MVVM).
 */
@Composable
fun UbicacionPredioSection(
    latitud: Double?,
    longitud: Double?,
    onUbicacionObtenida: (latitud: Double, longitud: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var mostrarAjustesApp by remember { mutableStateOf(false) }
    var mostrarAjustesGps by remember { mutableStateOf(false) }

    fun buscarUbicacion() {
        error = null
        mostrarAjustesApp = false
        mostrarAjustesGps = false
        if (!UbicacionHelper.ubicacionActivada(context)) {
            error = "La ubicación del teléfono está apagada. Actívala para continuar."
            mostrarAjustesGps = true
            return
        }
        cargando = true
        scope.launch {
            val ubicacion = UbicacionHelper.obtenerUbicacion(context)
            cargando = false
            if (ubicacion != null) {
                onUbicacionObtenida(ubicacion.latitude, ubicacion.longitude)
            } else {
                error = "No se pudo obtener la ubicación. Sal a un lugar abierto e inténtalo de nuevo."
            }
        }
    }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) {
            buscarUbicacion()
        } else {
            error = "Sin el permiso de ubicación no podemos registrar dónde está el predio."
            mostrarAjustesApp = true
        }
    }

    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Ubicación del predio", style = MaterialTheme.typography.titleMedium)
            }

            AnimatedVisibility(
                visible = latitud != null && longitud != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Text("Latitud: ${formatear(latitud)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Longitud: ${formatear(longitud)}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = {
                    if (UbicacionHelper.tienePermiso(context)) buscarUbicacion()
                    else pedirPermiso.launch(UbicacionHelper.PERMISOS)
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Buscando ubicación…")
                } else {
                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (latitud == null) "Usar mi ubicación" else "Actualizar ubicación")
                }
            }

            AnimatedVisibility(visible = error != null) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (mostrarAjustesApp) {
                        TextButton(onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }) {
                            Icon(Icons.Filled.Settings, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Dar permiso en Ajustes")
                        }
                    }
                    if (mostrarAjustesGps) {
                        TextButton(onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }) {
                            Icon(Icons.Filled.Settings, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Activar ubicación")
                        }
                    }
                }
            }
        }
    }
}

private fun formatear(valor: Double?): String =
    if (valor == null) "—" else String.format(Locale.US, "%.6f", valor)
