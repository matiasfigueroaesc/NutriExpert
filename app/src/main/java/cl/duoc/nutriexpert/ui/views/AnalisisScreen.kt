
package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.nutriexpert.data.local.AnalisisEntity
import cl.duoc.nutriexpert.viewmodel.AnalisisViewModel

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

    val analisisDelSector = analisis.filter {
        it.sectorId == sectorId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Análisis de suelo y agua",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = onCrearAnalisis,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nuevo análisis")
        }

        if (analisisDelSector.isEmpty()) {
            Text("Este sector todavía no tiene análisis registrados.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = analisisDelSector,
                    key = { it.id }
                ) { item ->
                    AnalisisCard(item)
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

@Composable
private fun AnalisisCard(analisis: AnalisisEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Análisis del ${analisis.fecha}",
                style = MaterialTheme.typography.titleMedium
            )

            Text("Profundidad: ${analisis.profundidadCm} cm")
            Text("Densidad aparente: ${analisis.densidadAparente}")
            Text("Pedregosidad: ${analisis.pedregosidadPct}%")

            HorizontalDivider()

            Text("Suelo (ppm)", style = MaterialTheme.typography.titleSmall)
            Text("N: ${analisis.sueloN} | P: ${analisis.sueloP} | K: ${analisis.sueloK}")
            Text("Ca: ${analisis.sueloCa} | Mg: ${analisis.sueloMg}")

            HorizontalDivider()

            Text("Agua (mg/L)", style = MaterialTheme.typography.titleSmall)
            Text("N: ${analisis.aguaN} | P: ${analisis.aguaP} | K: ${analisis.aguaK}")
            Text("Ca: ${analisis.aguaCa} | Mg: ${analisis.aguaMg}")
            Text("Agua aplicada: ${analisis.aguaM3Ha} m³/ha")
        }
    }
}
