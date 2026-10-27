package cl.duoc.nutriexpert.ui.plan

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.domain.model.Nutriente
import cl.duoc.nutriexpert.domain.motor.Distribucion
import cl.duoc.nutriexpert.viewmodel.PlanViewModel
import cl.duoc.nutriexpert.viewmodel.aNumero

// Paso 4 del asistente: % de la dosis en cada etapa, por nutriente.
@Composable
fun PlanDistribucionScreen(navController: NavHostController, planViewModel: PlanViewModel) {
    val estado = planViewModel.estado
    val nutriente = estado.nutrienteVista
    val etapas = planViewModel.etapas()
    val ha = aNumero(estado.hectareas) ?: 0.0
    // Fertilizante que cubre el nutriente elegido (puede no haber)
    val dosis = planViewModel.resultado().dosis.firstOrNull { it.nutrienteCubierto == nutriente }

    Scaffold(
        topBar = { PlanTopBar("Paso 4 · Distribución", navController) },
        bottomBar = {
            BotonesPaso(onAtras = { navController.popBackStack() }) {
                navController.navigate("plan/estanques")
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
            IndicadorPaso(4)

            // Chips para elegir qué nutriente se está repartiendo
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Nutriente.entries.forEach { n ->
                    FilterChip(
                        selected = n == nutriente,
                        onClick = { planViewModel.onNutrienteVistaChange(n) },
                        label = { Text(n.etiqueta) }
                    )
                }
            }

            Text(
                text = dosis?.let { "${it.fertilizante.nombre} · ${formato(it.kgHa)} kg/ha" }
                    ?: "Sin fertilizante para este nutriente",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            etapas.forEachIndexed { i, etapa ->
                val porcentaje = etapa.porcentaje(nutriente)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row {
                            Text(
                                text = etapa.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${formato1(porcentaje)} %",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "${etapa.semanas} semanas · ${etapa.riegosSemana} riegos/semana",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        // Al mover una etapa, las demás se reajustan para sumar 100 %
                        Slider(
                            value = porcentaje.toFloat(),
                            onValueChange = { planViewModel.onPorcentajeChange(i, it.toDouble()) },
                            valueRange = 0f..100f
                        )
                        if (dosis != null) {
                            val kgEtapa = Distribucion.kgEtapa(dosis.kgHa, porcentaje, ha)
                            val kgRiego = Distribucion.kgPorRiego(kgEtapa, etapa.semanas, etapa.riegosSemana)
                            Text(
                                text = "Etapa (sector): ${formato(kgEtapa)} kg · Por riego: ${formato(kgRiego)} kg",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Text(
                                text = "Sin fertilizante para este nutriente",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Text(
                text = "Total: ${formato1(etapas.sumOf { it.porcentaje(nutriente) }).removeSuffix(",0")} %",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
