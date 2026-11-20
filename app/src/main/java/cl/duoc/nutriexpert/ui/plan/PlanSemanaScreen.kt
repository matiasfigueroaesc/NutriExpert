
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
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.domain.model.Nutriente
import cl.duoc.nutriexpert.viewmodel.PlanViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val VOLUMENES = listOf(500.0, 1000.0, 2000.0)

@Composable
fun PlanSemanaScreen(
    navController: NavHostController,
    planViewModel: PlanViewModel,
    onGuardarPlan: suspend (String, String, String) -> Long
) {
    val estado = planViewModel.estado
    val etapas = planViewModel.etapas()
    val etapa = etapas[estado.etapaEstanque]
    val estanques = planViewModel.estanques()

    var nombreTemporada by remember {
        mutableStateOf("")
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    var errorGuardado by remember {
        mutableStateOf<String?>(null)
    }

    var fechaInicio by remember { mutableStateOf<LocalDate?>(null) }
    var fechaTermino by remember { mutableStateOf<LocalDate?>(null) }
    val context = LocalContext.current
    val semanasCultivo = etapas.sumOf { it.semanas }
    val diasTemporada = if (fechaInicio != null && fechaTermino != null) {
        ChronoUnit.DAYS.between(fechaInicio, fechaTermino) + 1
    } else null

    fun abrirCalendario(esInicio: Boolean) {
        val actual = if (esInicio) fechaInicio else fechaTermino
        val hoy = actual ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, anio, mes, dia ->
                val fecha = LocalDate.of(anio, mes + 1, dia)
                if (esInicio) fechaInicio = fecha else fechaTermino = fecha
            },
            hoy.year,
            hoy.monthValue - 1,
            hoy.dayOfMonth
        ).show()
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        planViewModel.onMostrarAjustesChange(false)
    }

    Scaffold(
        topBar = {
            PlanTopBar("Nuevo plan nutricional", navController)
        },
        bottomBar = {
            BotonesPaso(
                onAtras = {
                    if (!guardando) {
                        navController.popBackStack()
                    }
                },
                textoSiguiente = if (guardando) "Guardando..." else "Listo"
            ) {
                if (!guardando) {
                    if (nombreTemporada.isBlank()) {
                        errorGuardado = "Ingresa un nombre para la temporada"
                    } else if (fechaInicio == null || fechaTermino == null) {
                        errorGuardado = "Selecciona las fechas de inicio y término"
                    } else if (fechaTermino!!.isBefore(fechaInicio)) {
                        errorGuardado = "La fecha de término debe ser posterior al inicio"
                    } else {
                        guardando = true
                        errorGuardado = null

                        scope.launch {
                            try {
                                onGuardarPlan(nombreTemporada.trim(), fechaInicio.toString(), fechaTermino.toString())

                                navController.popBackStack(
                                    "plan/cultivo",
                                    inclusive = true
                                )
                            } catch (e: Exception) {
                                errorGuardado =
                                    "No se pudo guardar el plan. Inténtalo nuevamente."
                            } finally {
                                guardando = false
                            }
                        }
                    }
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

            IndicadorPaso(4, "Así lo preparas cada semana")

            Text(
                text = "Nombre de la temporada",
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = nombreTemporada,
                onValueChange = {
                    nombreTemporada = it
                    errorGuardado = null
                },
                label = {
                    Text("Ejemplo: Tomate primavera")
                },
                singleLine = true,
                isError = errorGuardado != null,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorGuardado != null) {
                Text(
                    text = errorGuardado.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "Etapa",
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                etapas.forEachIndexed { i, e ->
                    FilterChip(
                        selected = i == estado.etapaEstanque,
                        onClick = {
                            planViewModel.onEtapaEstanqueChange(i)
                        },
                        label = {
                            Text(e.nombre)
                        }
                    )
                }
            }

            Text(
                text = "Tamaño del estanque",
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VOLUMENES.forEach { litros ->
                    FilterChip(
                        selected = litros == estado.volumenL,
                        onClick = {
                            planViewModel.onVolumenChange(litros)
                        },
                        label = {
                            Text("${formatoCorto(litros)} L")
                        }
                    )
                }
            }

            Text(
                text = "En «${etapa.nombre}» (${etapa.semanas} semanas, " +
                        "${etapa.riegosSemana} riegos por semana), " +
                        "cada semana prepara:",
                fontSize = 16.sp
            )

            if (estanques.isEmpty()) {
                Text("No hay fertilizantes para esta etapa.")
            }

            estanques.forEach { estanque ->

                val fondo = if (estanque.sobreLimite) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }

                val texto = if (estanque.sobreLimite) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = fondo
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Estanque ${estanque.nombre} · llénalo con " +
                                    "${formatoCorto(estanque.volumenL)} L de agua",
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
                            text = "Concentración " +
                                    "${formato1(estanque.concentracionPct)} % " +
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

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "El calcio (estanque A) nunca se mezcla " +
                            "con sulfatos ni fosfatos (estanque B).",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            SeccionAjustes(
                abierta = estado.mostrarAjustes,
                onAbiertaChange = {
                    planViewModel.onMostrarAjustesChange(it)
                }
            ) {
                val nutriente = estado.nutrienteVista

                Text(
                    text = "Reparto por etapa",
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Nutriente.entries.forEach { n ->
                        FilterChip(
                            selected = n == nutriente,
                            onClick = {
                                planViewModel.onNutrienteVistaChange(n)
                            },
                            label = {
                                Text(nombreNutrienteTitulo(n))
                            }
                        )
                    }
                }

                etapas.forEachIndexed { i, e ->
                    val porcentaje = e.porcentaje(nutriente)

                    Column {
                        Row {
                            Text(
                                text = e.nombre,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "${formato1(porcentaje)} %",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = porcentaje.toFloat(),
                            onValueChange = {
                                planViewModel.onPorcentajeChange(
                                    i,
                                    it.toDouble()
                                )
                            },
                            valueRange = 0f..100f
                        )
                    }
                }

                Text(
                    text = "Suma: ${
                        formato1(
                            etapas.sumOf {
                                it.porcentaje(nutriente)
                            }
                        ).removeSuffix(",0")
                    } %",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Concentración máxima: " +
                            "${estado.cmaxPct.roundToInt()} %",
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = estado.cmaxPct.toFloat(),
                    onValueChange = {
                        planViewModel.onCmaxChange(
                            it.roundToInt().toDouble()
                        )
                    },
                    valueRange = 5f..30f,
                    steps = 24
                )
            }
        }
    }
}
