package cl.duoc.nutriexpert.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Aviso chico "Pendiente de sincronizar": el dato está guardado en el teléfono,
// pero el servidor todavía no lo tiene (por ejemplo, porque no hay internet).
@Composable
fun PendienteSincronizar(modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = "Pendiente de sincronizar",
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Pendiente de sincronizar",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}
