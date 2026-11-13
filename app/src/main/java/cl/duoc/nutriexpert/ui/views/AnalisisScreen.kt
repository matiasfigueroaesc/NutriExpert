
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.nutriexpert.data.local.AnalisisEntity
import cl.duoc.nutriexpert.viewmodel.AnalisisViewModel

// Lista de análisis de un sector. Usa la misma barra verde y botón flotante que "Mis predios".
@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Análisis de suelo y agua") },
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
            if (analisisDelSector.isEmpty()) {
                Text(
                    text = "Este sector todavía no tiene análisis. Toca \"Nuevo análisis\" para agregar el primero.",
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
                        items = analisisDelSector,
                        key = { it.id }
                    ) { item ->
                        AnalisisCard(item)
                    }
                }
            }
        }
    }
}

// Tarjeta de un análisis, con los mismos colores que la tarjeta de un predio
@Composable
private fun AnalisisCard(analisis: AnalisisEntity) {
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
            Text(
                text = "Análisis del ${analisis.fecha}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
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
