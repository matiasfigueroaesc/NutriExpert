
package cl.duoc.nutriexpert.ui.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.nutriexpert.data.local.SectorEntity
import cl.duoc.nutriexpert.ui.components.BarraSuperior
import cl.duoc.nutriexpert.ui.components.BotonPrincipal
import cl.duoc.nutriexpert.ui.components.BotonSecundario
import cl.duoc.nutriexpert.ui.components.PendienteSincronizar
import cl.duoc.nutriexpert.ui.plan.formatoCorto
import cl.duoc.nutriexpert.viewmodel.SectoresViewModel

// Pantalla "Sectores del predio": lista los sectores guardados en Room.
// Mismo estilo que "Mis predios": barra verde, botón flotante y tarjetas.
@Composable
fun SectoresScreen(
    predioId: Int,
    viewModel: SectoresViewModel,
    onCrearSector: () -> Unit,
    onVerAnalisis: (Int) -> Unit,
    onCrearPlan: (SectorEntity) -> Unit,
    onVerPlanes: (Int) -> Unit,
    onVolver: () -> Unit,
    onEditarSector: (Int) -> Unit = {}
) {
    // Al abrir la pantalla, carga desde Room los sectores de este predio
    LaunchedEffect(predioId) {
        viewModel.cargarSectores(predioId)
    }

    val sectores by viewModel.sectores.collectAsState()
    val sectoresDelPredio = sectores.filter { it.predioId == predioId }
    // Sector que el usuario quiere borrar (null = no se muestra el diálogo)
    var sectorAEliminar by remember { mutableStateOf<SectorEntity?>(null) }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Sectores del predio", onVolver = onVolver) },
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
            Text(
                text = "${sectoresDelPredio.size} sector(es) · " +
                    "${formatoCorto(sectoresDelPredio.sumOf { it.hectareas })} ha",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            AnimatedVisibility(
                visible = sectoresDelPredio.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Este predio todavía no tiene sectores. Toca \"Nuevo sector\" para agregar el primero.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                // Espacio abajo para que el botón flotante no tape la última tarjeta
                contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
            ) {
                items(items = sectoresDelPredio, key = { it.id }) { sector ->
                    SectorCard(
                        sector = sector,
                        onVerAnalisis = { onVerAnalisis(sector.id) },
                        onCrearPlan = { onCrearPlan(sector) },
                        onEditar = { onEditarSector(sector.id) },
                        onEliminar = { sectorAEliminar = sector },
                        onVerPlanes = { onVerPlanes(sector.id) },
                        onEditarSuperficie = { hectareas ->
                            viewModel.actualizarHectareas(sector.id, hectareas) { }
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    // Diálogo para confirmar antes de borrar (igual que en Mis predios)
    sectorAEliminar?.let { sector ->
        AlertDialog(
            onDismissRequest = { sectorAEliminar = null },
            title = { Text("Eliminar sector") },
            text = { Text("¿Seguro que quieres eliminar \"${sector.nombre}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarSector(sector) { }
                        sectorAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { sectorAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SectorCard(
    sector: SectorEntity,
    onVerAnalisis: () -> Unit,
    onCrearPlan: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onVerPlanes: () -> Unit,
    onEditarSuperficie: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val nombreCultivo = when (sector.cultivoId) {
        "cultivo-tomate" -> "Tomate"
        "cultivo-lechuga" -> "Lechuga"
        else -> sector.cultivoId
    }

    var mostrarEdicion by remember { mutableStateOf(false) }
    var superficieTexto by remember(sector.hectareas) {
        mutableStateOf(
            String.format(Locale.US, "%.3f", sector.hectareas)
                .replace('.', ',')
                .trimEnd('0')
                .trimEnd(',')
        )
    }
    var errorSuperficie by remember { mutableStateOf<String?>(null) }

    if (mostrarEdicion) {
        AlertDialog(
            onDismissRequest = { mostrarEdicion = false },
            title = { Text("Editar superficie") },
            text = {
                Column {
                    Text("Ingresa la superficie en hectáreas.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = superficieTexto,
                        onValueChange = {
                            superficieTexto = it
                            errorSuperficie = null
                        },
                        label = { Text("Superficie (ha)") },
                        placeholder = { Text("Ejemplo: 2,555") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        isError = errorSuperficie != null,
                        supportingText = {
                            errorSuperficie?.let { Text(it) }
                        },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val numero = if (superficieTexto.contains('.')) {
                            null
                        } else {
                            superficieTexto.replace(',', '.').toDoubleOrNull()
                        }

                        if (numero == null || !numero.isFinite() || numero <= 0.0) {
                            errorSuperficie = "Ingresa un número mayor a cero usando coma decimal"
                        } else {
                            onEditarSuperficie(numero)
                            mostrarEdicion = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarEdicion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sector.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = nombreCultivo,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (!sector.sincronizado) {
                        PendienteSincronizar()
                    }
                }
                Text(
                    text = "${formatoCorto(sector.hectareas)} ha",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            BotonSecundario(
                texto = "Editar superficie",
                onClick = {
                    superficieTexto = sector.hectareas.toString().replace('.', ',')
                    errorSuperficie = null
                    mostrarEdicion = true
                }
            )

            BotonPrincipal(texto = "Crear plan", onClick = onCrearPlan)
            BotonSecundario(
                texto = "Mis planes",
                onClick = onVerPlanes
            )
            BotonSecundario(
                texto = "Análisis de suelo y agua",
                onClick = onVerAnalisis,
                icono = Icons.Filled.Info
            )
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onEditar) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }
                TextButton(onClick = onEliminar) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}


