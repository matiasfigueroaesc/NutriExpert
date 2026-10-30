package cl.duoc.nutriexpert.ui.plan

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.domain.model.Nutriente
import cl.duoc.nutriexpert.viewmodel.PlanViewModel
import kotlin.math.roundToInt

// Volúmenes de estanque que se pueden elegir (litros)
private val VOLUMENES = listOf(500.0, 1000.0, 2000.0)

// Paso 4 del asistente: qué echar a cada estanque cada semana de la etapa elegida.
@Composable
fun PlanSemanaScreen(navController: NavHostController, planViewModel: PlanViewModel) {
    val estado = planViewModel.estado
    val etapas = planViewModel.etapas()
    val etapa = etapas[estado.etapaEstanque]
    val estanques = planViewModel.estanques()
    // Los ajustes del asesor parten cerrados cada vez que se entra a la pantalla
    LaunchedEffect(Unit) { planViewModel.onMostrarAjustesChange(false) }

    Scaffold(
        topBar = { PlanTopBar("Nuevo plan nutricional", navController) },
        bottomBar = {
            BotonesPaso(
                onAtras = { navController.popBackStack() },
                textoSiguiente = "Listo"
            ) {
                // Sale del asistente y vuelve a Mis predios
                navController.popBackStack("plan/cultivo", inclusive = true)
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
            IndicadorPaso(4, "Así lo preparas cada semana")

            Text(text = "Etapa", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                etapas.forEachIndexed { i, e ->
                    FilterChip(
                        selected = i == estado.etapaEstanque,
                        onClick = { planViewModel.onEtapaEstanqueChange(i) },
                        label = { Text(e.nombre) }
                    )
                }
            }

            Text(text = "Tamaño del estanque", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VOLUMENES.forEach { litros ->
                    FilterChip(
                        selected = litros == estado.volumenL,
                        onClick = { planViewModel.onVolumenChange(litros) },
                        label = { Text("${formatoCorto(litros)} L") }
                    )
                }
            }

            Text(
                text = "En «${etapa.nombre}» (${etapa.semanas} semanas, ${etapa.riegosSemana} riegos por semana), " +
                    "cada semana prepara:",
                fontSize = 16.sp
            )

            if (estanques.isEmpty()) {
                Text("No hay fertilizantes para esta etapa.")
            }

            estanques.forEach { estanque ->
                // Si está muy concentrado, la tarjeta se marca en rojo
                val fondo = if (estanque.sobreLimite) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer
                val texto = if (estanque.sobreLimite) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onPrimaryContainer
                Card(
                    colors = CardDefaults.cardColors(containerColor = fondo),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Estanque ${estanque.nombre} · llénalo con ${formatoCorto(estanque.volumenL)} L de agua",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = texto
                        )
                        estanque.items.forEach { (nombre, kg) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = nombre,
                                    fontSize = 16.sp,
                                    color = texto,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${formato(kg)} kg",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = texto
                                )
                            }
                        }
                        Text(
                            text = "Concentración ${formato1(estanque.concentracionPct)} % " +
                                "(máximo ${estanque.cmaxPct.roundToInt()} %)",
                            fontSize = 13.sp,
                            color = texto,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        if (estanque.sobreLimite) {
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Muy concentrado: usa un estanque más grande",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Regla fija de mezcla
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "El calcio (estanque A) nunca se mezcla con sulfatos ni fosfatos (estanque B).",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Curvas por etapa y concentración máxima: solo las cambia el asesor
            SeccionAjustes(
                abierta = estado.mostrarAjustes,
                onAbiertaChange = { planViewModel.onMostrarAjustesChange(it) }
            ) {
                val nutriente = estado.nutrienteVista
                Text(text = "Reparto por etapa", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Nutriente.entries.forEach { n ->
                        FilterChip(
                            selected = n == nutriente,
                            onClick = { planViewModel.onNutrienteVistaChange(n) },
                            label = { Text(nombreNutrienteTitulo(n)) }
                        )
                    }
                }
                // Al mover una etapa, las demás se reajustan para sumar 100 %
                etapas.forEachIndexed { i, e ->
                    val porcentaje = e.porcentaje(nutriente)
                    Column {
                        Row {
                            Text(text = e.nombre, modifier = Modifier.weight(1f))
                            Text(text = "${formato1(porcentaje)} %", fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = porcentaje.toFloat(),
                            onValueChange = { planViewModel.onPorcentajeChange(i, it.toDouble()) },
                            valueRange = 0f..100f
                        )
                    }
                }
                Text(
                    text = "Suma: ${formato1(etapas.sumOf { it.porcentaje(nutriente) }).removeSuffix(",0")} %",
                    fontWeight = FontWeight.Bold
                )

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
            }
        }
    }
}
