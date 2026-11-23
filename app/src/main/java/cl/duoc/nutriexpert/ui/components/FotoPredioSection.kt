package cl.duoc.nutriexpert.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.nutriexpert.nativo.FotoHelper
import kotlinx.coroutines.launch

/**
 * Sección "Foto del predio" para el formulario: tomar foto con la cámara
 * o elegirla de la galería, y ver la vista previa.
 *
 * Avisa la ruta nueva (o null si se quitó) con [onFotoSeleccionada];
 * el ViewModel la guarda en el campo fotoUri del predio y decide cuándo
 * borrar la foto anterior (recién al guardar el predio).
 */
@Composable
fun FotoPredioSection(
    fotoUri: String?,
    onFotoSeleccionada: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Ruta del archivo que la cámara está llenando (sobrevive a rotar la pantalla).
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var mostrarAjustes by remember { mutableStateOf(false) }

    val vistaPrevia by produceState<ImageBitmap?>(initialValue = null, fotoUri) {
        value = fotoUri?.let { FotoHelper.cargarVistaPrevia(context, it) }
    }

    // La foto anterior no se borra aquí: si el usuario sale sin guardar, debe seguir existiendo
    fun reemplazarFoto(nueva: String) {
        onFotoSeleccionada(nueva)
    }

    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val pendiente = uriPendiente
        uriPendiente = null
        if (pendiente == null) return@rememberLauncherForActivityResult
        if (exito) reemplazarFoto(pendiente) else FotoHelper.borrar(context, pendiente)
    }

    fun abrirCamara() {
        error = null
        mostrarAjustes = false
        try {
            val uri = FotoHelper.crearUriParaFoto(context)
            uriPendiente = uri.toString()
            tomarFoto.launch(uri)
        } catch (e: ActivityNotFoundException) {
            FotoHelper.borrar(context, uriPendiente)
            uriPendiente = null
            error = "Este teléfono no tiene una app de cámara disponible."
        }
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            error = "Sin el permiso de cámara no podemos sacar la foto del predio."
            mostrarAjustes = true
        }
    }

    // La galería (Photo Picker) no necesita permiso.
    val elegirDeGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val copia = FotoHelper.copiarDesdeGaleria(context, uri)
                if (copia != null) reemplazarFoto(copia.toString())
                else error = "No se pudo usar esa imagen. Prueba con otra."
            }
        }
    }

    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Foto del predio", style = MaterialTheme.typography.titleMedium)

            AnimatedVisibility(
                visible = vistaPrevia != null,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut(targetScale = 0.9f)
            ) {
                vistaPrevia?.let { imagen ->
                    Image(
                        bitmap = imagen,
                        contentDescription = "Vista previa de la foto del predio",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            if (fotoUri == null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Aún no hay foto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (FotoHelper.tienePermisoCamara(context)) abrirCamara()
                        else pedirPermisoCamara.launch(FotoHelper.PERMISO_CAMARA)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Tomar foto")
                }
                OutlinedButton(
                    onClick = {
                        error = null
                        elegirDeGaleria.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Galería")
                }
            }

            if (fotoUri != null) {
                TextButton(onClick = { onFotoSeleccionada(null) }) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Quitar foto")
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
                    if (mostrarAjustes) {
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
                }
            }
        }
    }
}
