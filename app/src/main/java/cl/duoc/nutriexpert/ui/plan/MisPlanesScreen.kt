package cl.duoc.nutriexpert.ui.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.nutriexpert.data.local.PlanEntity
import cl.duoc.nutriexpert.ui.components.BarraSuperior
import cl.duoc.nutriexpert.viewmodel.PlanesViewModel

@Composable
fun MisPlanesScreen(
    sectorId: Int,
    viewModel: PlanesViewModel,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    var planEditar by remember { mutableStateOf<PlanEntity?>(null) }
    var planEliminar by remember { mutableStateOf<PlanEntity?>(null) }
    var nuevoNombre by remember { mutableStateOf("") }

    LaunchedEffect(sectorId) {
        viewModel.cargarPlanes(sectorId)
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Mis planes",
                onVolver = onVolver
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (estado.cargando) {
                CircularProgressIndicator()
            }

            estado.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (!estado.cargando && estado.planes.isEmpty()) {
                Text("Este sector todavía no tiene planes guardados.")
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(estado.planes, key = { it.id }) { plan ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                plan.nombreTemporada,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                if (plan.sincronizado)
                                    "Sincronizado"
                                else
                                    "Pendiente de sincronizar"
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        nuevoNombre = plan.nombreTemporada
                                        planEditar = plan
                                    }
                                ) {
                                    Text("Editar")
                                }

                                OutlinedButton(
                                    onClick = { planEliminar = plan }
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    planEditar?.let { plan ->
        AlertDialog(
            onDismissRequest = { planEditar = null },
            title = { Text("Cambiar nombre") },
            text = {
                OutlinedTextField(
                    value = nuevoNombre,
                    onValueChange = { nuevoNombre = it },
                    label = { Text("Nombre de temporada") }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = nuevoNombre.isNotBlank(),
                    onClick = {
                        viewModel.editarPlan(plan.id, nuevoNombre)
                        planEditar = null
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { planEditar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    planEliminar?.let { plan ->
        AlertDialog(
            onDismissRequest = { planEliminar = null },
            title = { Text("Eliminar plan") },
            text = {
                Text("¿Eliminar el plan ${plan.nombreTemporada}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarPlan(plan.id)
                        planEliminar = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { planEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
