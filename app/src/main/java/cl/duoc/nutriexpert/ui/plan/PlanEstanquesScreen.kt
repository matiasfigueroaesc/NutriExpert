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
import cl.duoc.nutriexpert.viewmodel.PlanViewModel
import kotlin.math.roundToInt

// Volúmenes de estanque que se pueden elegir (litros)
private val VOLUMENES = listOf(500.0, 1000.0, 2000.0)

// Paso 5 del asistente: armado de estanques A y B para una semana de la etapa elegida.
@Composable
fun PlanEstanquesScreen(navController: NavHostController, planViewModel: PlanViewModel) {
    val estado = planViewModel.estado
    val etapas = planViewModel.etapas()
    val estanques = planViewModel.estanques()

    Scaffold(
        topBar = { PlanTopBar("Paso 5 · Estanques", navController) },
        bottomBar = {
            BotonesPaso(
                onAtras = { navController.popBackStack() },
                textoSiguiente = "Terminar"
            ) {
                // Sale del asistente y vuelve a la pantalla desde donde se entró
                navController.popBackStack("plan/meta", inclusive = true)
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
            IndicadorPaso(5)

            Text(text = "Etapa", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                etapas.forEachIndexed { i, etapa ->
                    FilterChip(
                        selected = i == estado.etapaEstanque,
                        onClick = { planViewModel.onEtapaEstanqueChange(i) },
                        label = { Text(etapa.nombre) }
                    )
                }
            }

            Text(text = "Volumen del estanque", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VOLUMENES.forEach { litros ->
                    FilterChip(
                        selected = litros == estado.volumenL,
                        onClick = { planViewModel.onVolumenChange(litros) },
                        label = { Text("${litros.toInt()} L") }
                    )
                }
            }

            Text(
                text = "Concentración máxima: ${estado.cmaxPct.roundToInt()} %",
                fontWeight = FontWeight.Bold
            )
            // 5 a 30 %, de a 1 %
            Slider(
                value = estado.cmaxPct.toFloat(),
                onValueChange = { planViewModel.onCmaxChange(it.roundToInt().toDouble()) },
                valueRange = 5f..30f,
                steps = 24
            )

            Text(
                text = "El calcio (A) nunca se mezcla con sulfatos ni fosfatos (B)",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            if (estanques.isEmpty()) {
                Text("No hay fertilizantes para esta etapa.")
            }

            estanques.forEach { estanque ->
                // Si supera la concentración máxima, la tarjeta se marca en rojo
                val fondo = if (estanque.sobreLimite) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer
                val texto = if (estanque.sobreLimite) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onPrimaryContainer
                Card(
                    colors = CardDefaults.cardColors(containerColor = fondo),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row {
                            Text(
                                text = "Estanque ${estanque.nombre}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = texto,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Grupo ${estanque.grupo.etiqueta}",
                                fontSize = 14.sp,
                                color = texto
                            )
                        }
                        Text(
                            text = "${formato(estanque.kgTotal)} kg · ${formato1(estanque.concentracionPct)} %",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (estanque.sobreLimite) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                        if (estanque.sobreLimite) {
                            Text(
                                text = "Supera la concentración máxima",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        estanque.items.forEach { (nombre, kg) ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = nombre,
                                    fontSize = 14.sp,
                                    color = texto,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(text = "${formato(kg)} kg", fontSize = 14.sp, color = texto)
                            }
                        }
                    }
                }
            }
        }
    }
}
