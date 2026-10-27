package cl.duoc.nutriexpert.ui.plan

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.domain.model.Nutriente
import cl.duoc.nutriexpert.viewmodel.PlanViewModel

// Paso 3 del asistente: dosis por fertilizante (cálculo inverso) y alertas.
@Composable
fun PlanDosisScreen(navController: NavHostController, planViewModel: PlanViewModel) {
    val estado = planViewModel.estado
    val resultado = planViewModel.resultado()
    val deficits = resultado.deficits()
    val excesos = resultado.excesos()

    Scaffold(
        topBar = { PlanTopBar("Paso 3 · Dosis", navController) },
        bottomBar = {
            BotonesPaso(onAtras = { navController.popBackStack() }) {
                navController.navigate("plan/distribucion")
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
            IndicadorPaso(3)

            // Alerta: hay nutrientes con meta y sin fertilizante que los aporte
            if (deficits.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Falta fuente para: " + deficits.entries.joinToString(", ") {
                                "${it.key.etiqueta} ${formato(it.value)} kg/ha"
                            },
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "El plan no se puede activar",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Aviso: algún nutriente queda sobre la meta (solo informa, no cambia dosis)
            if (excesos.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Exceso sobre la meta: " + excesos.entries.joinToString(", ") {
                            "${it.key.etiqueta} ${formato(it.value)} kg/ha"
                        },
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            resultado.dosis.forEach { d ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = d.fertilizante.nombre,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Cubre ${d.nutrienteCubierto.etiqueta}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "${formato(d.kgHa)} kg/ha",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        // Detalle: cuánto aporta este fertilizante a cada nutriente
                        AnimatedVisibility(visible = estado.mostrarDetalle) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Nutriente.entries
                                    .filter { d.fertilizante.contenidoDe(it) > 0.0 }
                                    .forEach { n ->
                                        val aporte = d.kgHa * d.fertilizante.contenidoDe(n) / 100.0
                                        Text(
                                            text = "Aporta ${n.etiqueta}: ${formato(aporte)} kg/ha",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Total",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${formato(resultado.totalKgHa)} kg/ha",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Mostrar detalle", modifier = Modifier.weight(1f))
                Switch(
                    checked = estado.mostrarDetalle,
                    onCheckedChange = { planViewModel.onMostrarDetalleChange(it) }
                )
            }

            AnimatedVisibility(visible = estado.mostrarDetalle) {
                Text(
                    text = "N secundario (aportado por fuentes de otros nutrientes): ${formato(resultado.nSecundario)} kg/ha",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
