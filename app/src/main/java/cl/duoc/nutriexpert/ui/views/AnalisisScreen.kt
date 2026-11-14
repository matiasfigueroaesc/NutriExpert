
package cl.duoc.nutriexpert.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.nutriexpert.data.local.AnalisisEntity
import cl.duoc.nutriexpert.ui.components.BarraSuperior
import cl.duoc.nutriexpert.ui.plan.formatoCorto
import cl.duoc.nutriexpert.viewmodel.AnalisisViewModel

// Pantalla "Análisis de suelo y agua" de un sector.
// Mismo estilo que "Mis predios": barra verde, botón flotante y tarjetas.
@Composable
fun AnalisisScreen(
    sectorId: Int,
    viewModel: AnalisisViewModel,
    onCrearAnalisis: () -> Unit,
    onVolver: () -> Unit
) {
    // Al abrir la pantalla, carga desde Room los análisis de este sector
    LaunchedEffect(sectorId) {
        viewModel.cargarAnalisis(sectorId)
    }

    val analisis by viewModel.analisis.collectAsState()
    val analisisDelSector = analisis.filter { it.sectorId == sectorId }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Análisis de suelo y agua", onVolver = onVolver) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCrearAnalisis,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo análisis") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${analisisDelSector.size} análisis registrado(s)",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            AnimatedVisibility(
                visible = analisisDelSector.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Este sector todavía no tiene análisis. Toca \"Nuevo análisis\" para registrar el primero.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
            ) {
                items(items = analisisDelSector, key = { it.id }) { item ->
                    AnalisisCard(item, modifier = Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
private fun AnalisisCard(analisis: AnalisisEntity, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Análisis del ${fechaLegible(analisis.fecha)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Fila("Profundidad", "${formatoCorto(analisis.profundidadCm)} cm")
            Fila("Densidad aparente", "${formatoCorto(analisis.densidadAparente)} g/cm³")
            Fila("Pedregosidad", "${formatoCorto(analisis.pedregosidadPct)} %")

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Text("Suelo (ppm)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(
                "N ${formatoCorto(analisis.sueloN)} · P ${formatoCorto(analisis.sueloP)} · " +
                    "K ${formatoCorto(analisis.sueloK)} · Ca ${formatoCorto(analisis.sueloCa)} · " +
                    "Mg ${formatoCorto(analisis.sueloMg)}",
                color = color
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Text("Agua (mg/L)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(
                "N ${formatoCorto(analisis.aguaN)} · P ${formatoCorto(analisis.aguaP)} · " +
                    "K ${formatoCorto(analisis.aguaK)} · Ca ${formatoCorto(analisis.aguaCa)} · " +
                    "Mg ${formatoCorto(analisis.aguaMg)}",
                color = color
            )
            Fila("Agua aplicada", "${formatoCorto(analisis.aguaM3Ha)} m³/ha")
        }
    }
}

// Etiqueta a la izquierda y valor a la derecha, en una línea
@Composable
private fun Fila(etiqueta: String, valor: String) {
    val color = MaterialTheme.colorScheme.onPrimaryContainer
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, fontSize = 14.sp, color = color, modifier = Modifier.weight(1f))
        Text(valor, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// "2026-10-28" -> "28-10-2026", como se escriben las fechas en Chile
private fun fechaLegible(fecha: String): String {
    val partes = fecha.split("-")
    return if (partes.size == 3) "${partes[2]}-${partes[1]}-${partes[0]}" else fecha
}
