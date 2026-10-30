package cl.duoc.nutriexpert.ui.plan

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.viewmodel.PlanViewModel

// Paso 3 del asistente: cuántos kilos de cada fertilizante para todo el sector.
@Composable
fun PlanResumenScreen(navController: NavHostController, planViewModel: PlanViewModel) {
    val estado = planViewModel.estado
    val resultado = planViewModel.resultado()
    val ha = planViewModel.hectareasNumero()
    val deficits = resultado.deficits()
    val excesos = resultado.excesos()

    Scaffold(
        topBar = { PlanTopBar("Nuevo plan nutricional", navController) },
        bottomBar = {
            BotonesPaso(onAtras = { navController.popBackStack() }) {
                navController.navigate("plan/semana")
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
            IndicadorPaso(3, "Esto necesitas para la temporada")

            Text(
                text = "${estado.cultivo.nombre} · ${formatoCorto(ha)} ha",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            // Avisos en palabras simples: falta un nutriente, sobra uno, o está todo bien
            deficits.keys.forEach { n ->
                Aviso(
                    texto = "Te falta un fertilizante con ${nombreNutriente(n)}. " +
                        "Vuelve atrás y marca uno que lo tenga.",
                    icono = Icons.Filled.Warning,
                    fondo = MaterialTheme.colorScheme.errorContainer,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            excesos.forEach { (n, kgHa) ->
                Aviso(
                    texto = "Se aporta más ${nombreNutriente(n)} del necesario " +
                        "(${formato(kgHa * ha)} kg en el sector)",
                    icono = Icons.Filled.Info,
                    fondo = MaterialTheme.colorScheme.secondaryContainer,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            if (deficits.isEmpty() && excesos.isEmpty()) {
                Aviso(
                    texto = "Con estos fertilizantes cubres todo lo que necesita el cultivo.",
                    icono = Icons.Filled.CheckCircle,
                    fondo = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Una tarjeta por fertilizante con los kilos para todo el sector
            resultado.dosis.forEach { d ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = d.fertilizante.nombre,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${formato(d.kgHa * ha)} kg",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "para todo el sector",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        // Detalle técnico: dosis por hectárea y nutriente que cubre
                        AnimatedVisibility(visible = estado.mostrarDetalle) {
                            Text(
                                text = "${formato(d.kgHa)} kg/ha · cubre ${d.nutrienteCubierto.etiqueta} " +
                                    "(${nombreNutriente(d.nutrienteCubierto)})",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Total",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${formato(resultado.totalKgHa * ha)} kg",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Ver detalle técnico", modifier = Modifier.weight(1f))
                Switch(
                    checked = estado.mostrarDetalle,
                    onCheckedChange = { planViewModel.onMostrarDetalleChange(it) }
                )
            }

            AnimatedVisibility(visible = estado.mostrarDetalle) {
                Text(
                    text = "Nitrógeno que aportan otros fertilizantes (N secundario): " +
                        "${formato(resultado.nSecundario)} kg/ha",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

// Tarjeta de aviso con ícono y texto
@Composable
private fun Aviso(texto: String, icono: ImageVector, fondo: Color, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = fondo),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = texto, fontSize = 16.sp, color = color)
        }
    }
}
