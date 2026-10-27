package cl.duoc.nutriexpert.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.domain.model.Nutriente
import cl.duoc.nutriexpert.ui.components.CampoFormulario
import cl.duoc.nutriexpert.viewmodel.PlanViewModel

// Paso 1 del asistente: cultivo, superficie y demanda (kg/ha) -> meta neta.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanMetaScreen(navController: NavHostController, planViewModel: PlanViewModel) {
    val estado = planViewModel.estado
    // Si la lista de cultivos está abierta
    var menuAbierto by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { PlanTopBar("Paso 1 · Meta nutricional", navController) },
        bottomBar = {
            BotonesPaso(onAtras = null) {
                // Solo avanza si todos los campos están bien
                if (planViewModel.validarMeta()) navController.navigate("plan/fertilizantes")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IndicadorPaso(1)

            // Lista desplegable para elegir el cultivo
            ExposedDropdownMenuBox(
                expanded = menuAbierto,
                onExpandedChange = { menuAbierto = it }
            ) {
                OutlinedTextField(
                    value = estado.cultivo.nombre,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cultivo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    planViewModel.cultivos.forEach { cultivo ->
                        DropdownMenuItem(
                            text = { Text(cultivo.nombre) },
                            onClick = {
                                planViewModel.onCultivoChange(cultivo)
                                menuAbierto = false
                            }
                        )
                    }
                }
            }

            CampoFormulario(
                valor = estado.hectareas,
                onValorChange = { planViewModel.onHectareasChange(it) },
                etiqueta = "Superficie (ha)",
                error = estado.errorHectareas,
                tipoTeclado = KeyboardType.Decimal
            )

            Text(
                text = "Demanda del cultivo (kg/ha)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // Un campo por nutriente, cada uno con su propio error
            Nutriente.entries.forEach { n ->
                CampoFormulario(
                    valor = estado.demanda[n] ?: "",
                    onValorChange = { planViewModel.onDemandaChange(n, it) },
                    etiqueta = n.etiqueta,
                    error = estado.erroresDemanda[n],
                    tipoTeclado = KeyboardType.Decimal
                )
            }

            // Resultado: meta neta que tiene que cubrir el fertirriego
            val meta = planViewModel.meta()
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Meta neta (kg/ha)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Nutriente.entries.forEach { n ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = n.etiqueta,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = formato(meta.getValue(n)),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Text(
                text = "Sin análisis de suelo/agua por ahora",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
