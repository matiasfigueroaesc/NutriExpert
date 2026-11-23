package cl.duoc.nutriexpert.ui.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Delete
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
    var planVer by remember { mutableStateOf<PlanEntity?>(null) }
    var nuevoNombre by remember { mutableStateOf("") }
    var planesDesplegados by remember { mutableStateOf(setOf<Int>()) }
    var planExportar by remember { mutableStateOf<PlanEntity?>(null) }
    val contexto = LocalContext.current
    val crearPdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val plan = planExportar
        if (uri != null && plan != null) {
            try {
                exportarPlanPdf(contexto, uri, plan)
                Toast.makeText(contexto, "PDF guardado correctamente", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(contexto, "Error al guardar PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        planExportar = null
    }

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

                                val abierto = planesDesplegados.contains(plan.id)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text("Opciones", style = MaterialTheme.typography.labelMedium)
                                IconButton(onClick = { planesDesplegados = if (abierto) planesDesplegados - plan.id else planesDesplegados + plan.id }) {
                                    Icon(imageVector = if (abierto) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = if (abierto) "Ocultar opciones" else "Mostrar opciones")
                                }
                            }
                            if (abierto) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { planVer = plan }, modifier = Modifier.weight(1f)) { Text("Ver plan", maxLines = 1) }
                                        OutlinedButton(onClick = { planExportar = plan; crearPdf.launch("Plan_" + plan.id + ".pdf") }, modifier = Modifier.weight(1f)) { Text("Exportar PDF", maxLines = 1) }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        OutlinedButton(onClick = { nuevoNombre = plan.nombreTemporada; planEditar = plan }, modifier = Modifier.weight(1f)) { Text("Editar") }
                                        IconButton(onClick = { planEliminar = plan }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar plan", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    planVer?.let { plan ->
        AlertDialog(
            onDismissRequest = { planVer = null },
            title = { Text("Detalle del plan") },
            text = {
                Column(modifier = Modifier.heightIn(max = 450.dp)) {
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        Text(
                            text = textoDetallePlan(plan),
                            modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { planVer = null }) { Text("Cerrar") }
            }
        )
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






