package cl.duoc.nutriexpert.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.domain.model.Nutriente
import cl.duoc.nutriexpert.ui.components.CampoFormulario
import cl.duoc.nutriexpert.viewmodel.PlanViewModel

// Paso 1 del asistente: qué cultivo y cuántas hectáreas. La demanda se completa sola.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanCultivoScreen(navController: NavHostController, planViewModel: PlanViewModel) {
    val estado = planViewModel.estado
    // Si la lista de cultivos está abierta
    var menuAbierto by remember { mutableStateOf(false) }
    // Los ajustes del asesor parten cerrados cada vez que se entra a la pantalla
    LaunchedEffect(Unit) { planViewModel.onMostrarAjustesChange(false) }

    Scaffold(
        topBar = { PlanTopBar("Nuevo plan nutricional", navController) },
        bottomBar = {
            BotonesPaso(onAtras = null) {
                if (planViewModel.validarMeta()) {
                    navController.navigate("plan/fertilizantes")
                } else if (planViewModel.estado.erroresDemanda.isNotEmpty()) {
                    // El error está en los ajustes: se abren para que se vea
                    planViewModel.onMostrarAjustesChange(true)
                }
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
            IndicadorPaso(1, "¿Qué vas a fertilizar?")

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
                etiqueta = "Superficie del sector (hectáreas)",
                error = estado.errorHectareas,
                tipoTeclado = KeyboardType.Decimal
            )

            Text(
                text = "Lo que necesita el cultivo se completa solo.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            // Demanda del cultivo: solo la cambia el asesor
            SeccionAjustes(
                abierta = estado.mostrarAjustes,
                onAbiertaChange = { planViewModel.onMostrarAjustesChange(it) }
            ) {
                Text(
                    text = "Demanda del cultivo (kg/ha)",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                // Un campo por nutriente, cada uno con su propio error
                Nutriente.entries.forEach { n ->
                    CampoFormulario(
                        valor = estado.demanda[n] ?: "",
                        onValorChange = { planViewModel.onDemandaChange(n, it) },
                        etiqueta = "${n.etiqueta} · ${nombreNutriente(n)} (kg/ha)",
                        error = estado.erroresDemanda[n],
                        tipoTeclado = KeyboardType.Decimal
                    )
                }
            }
        }
    }
}
