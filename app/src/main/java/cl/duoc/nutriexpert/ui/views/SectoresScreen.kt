package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.nutriexpert.data.local.SectorEntity
import cl.duoc.nutriexpert.viewmodel.SectoresViewModel

// Lista de sectores de un predio. Usa la misma barra verde y botón flotante que "Mis predios".
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectoresScreen(
    predioId: Int,
    viewModel: SectoresViewModel,
    onCrearSector: () -> Unit,
    onVerAnalisis: (Int) -> Unit,
    onCrearPlan: (SectorEntity) -> Unit,
    onVolver: () -> Unit
) {
    // Al abrir la pantalla, carga desde Room los sectores de este predio
    LaunchedEffect(predioId) {
        viewModel.cargarSectores(predioId)
    }

    val sectores by viewModel.sectores.collectAsState()

    val sectoresDelPredio = sectores.filter {
        it.predioId == predioId
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sectores del predio") },
                navigationIcon = {
                    // Flecha atrás en vez del botón "Volver" de abajo
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCrearSector,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo sector") },
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

            // Resumen: cantidad de sectores y superficie total
            val totalHectareas = sectoresDelPredio.sumOf { it.hectareas }
            Text(
                text = "${sectoresDelPredio.size} sector(es) · ${"%.1f".format(totalHectareas)} ha",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            if (sectoresDelPredio.isEmpty()) {
                Text(
                    text = "Este predio todavía no tiene sectores. Toca \"Nuevo sector\" para agregar el primero.",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    // Espacio abajo para que el botón flotante no tape la última tarjeta
                    contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
                ) {
                    items(
                        items = sectoresDelPredio,
                        key = { it.id }
                    ) { sector ->
                        SectorCard(
                            sector = sector,
                            onVerAnalisis = {
                                onVerAnalisis(sector.id)
                            },
                            onCrearPlan = {
                                onCrearPlan(sector)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Tarjeta de un sector, con los mismos colores que la tarjeta de un predio
@Composable
private fun SectorCard(
    sector: SectorEntity,
    onVerAnalisis: () -> Unit,
    onCrearPlan: () -> Unit
) {
    val nombreCultivo = when (sector.cultivoId) {
        "cultivo-tomate" -> "Tomate"
        "cultivo-lechuga" -> "Lechuga"
        else -> sector.cultivoId
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nombre y cultivo a la izquierda, hectáreas a la derecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sector.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Cultivo: $nombreCultivo",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "${"%.1f".format(sector.hectareas)} ha",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = onCrearPlan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear plan")
            }

            TextButton(
                onClick = onVerAnalisis,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver análisis de suelo y agua")
            }
        }
    }
}
