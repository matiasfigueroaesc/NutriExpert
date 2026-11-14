package cl.duoc.nutriexpert.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Alto común de los botones de la app (el mismo que Login y el asistente del plan)
private val ALTO_BOTON = 50.dp

/**
 * Botón principal (relleno verde). Para la acción más importante de la pantalla.
 * Con [cargando] = true muestra una ruedita y no se puede volver a tocar.
 */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    cargando: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        modifier = modifier
            .fillMaxWidth()
            .height(ALTO_BOTON)
    ) {
        ContenidoBoton(texto, icono, cargando)
    }
}

/** Botón secundario (solo borde). Para acciones alternativas. */
@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ALTO_BOTON)
    ) {
        ContenidoBoton(texto, icono, cargando = false)
    }
}

@Composable
private fun ContenidoBoton(texto: String, icono: ImageVector?, cargando: Boolean) {
    if (cargando) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = LocalContentColor.current
        )
        Spacer(modifier = Modifier.width(10.dp))
    } else if (icono != null) {
        Icon(imageVector = icono, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
    }
    Text(texto, fontSize = 16.sp)
}
