
package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.nutriexpert.data.local.SectorEntity
import cl.duoc.nutriexpert.viewmodel.SectoresViewModel

@Composable
fun SectoresScreen(
    predioId: Int,
    viewModel: SectoresViewModel,
    onCrearSector: () -> Unit,
    onVerAnalisis: (Int) -> Unit,
    onVolver: () -> Unit
) {
    val sectores by viewModel.sectores.collectAsState()

    val sectoresDelPredio = sectores.filter {
        it.predioId == predioId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Sectores del predio",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = onCrearSector,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nuevo sector")
        }

        if (sectoresDelPredio.isEmpty()) {

            Text("Este predio todavía no tiene sectores.")

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = sectoresDelPredio,
                    key = { it.id }
                ) { sector ->

                    SectorCard(
                        sector = sector,
                        onVerAnalisis = {
                            onVerAnalisis(sector.id)
                        }
                    )
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
private fun SectorCard(
    sector: SectorEntity,
    onVerAnalisis: () -> Unit
) {

    val nombreCultivo = when (sector.cultivoId) {
        "cultivo-tomate" -> "Tomate"
        "cultivo-lechuga" -> "Lechuga"
        else -> sector.cultivoId
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = sector.nombre,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Cultivo: $nombreCultivo")

            Text("Superficie: ${sector.hectareas} ha")

            TextButton(
                onClick = onVerAnalisis,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver análisis de suelo y agua")
            }
        }
    }
}
