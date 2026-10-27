package cl.duoc.nutriexpert.ui.plan

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.domain.model.FertilizanteInfo
import cl.duoc.nutriexpert.domain.model.Nutriente
import cl.duoc.nutriexpert.viewmodel.PlanViewModel

// Composición del fertilizante en texto: "N 12 % · P₂O₅ 60 %"
fun composicion(fertilizante: FertilizanteInfo): String =
    Nutriente.entries
        .filter { fertilizante.contenidoDe(it) > 0.0 }
        .joinToString(" · ") { n ->
            val v = fertilizante.contenidoDe(n)
            val texto = if (v % 1.0 == 0.0) v.toInt().toString() else formato1(v)
            "${n.etiqueta} $texto %"
        }

// Paso 2 del asistente: marcar los fertilizantes que se pueden usar.
@Composable
fun PlanFertilizantesScreen(navController: NavHostController, planViewModel: PlanViewModel) {
    val estado = planViewModel.estado

    Scaffold(
        topBar = { PlanTopBar("Paso 2 · Fertilizantes", navController) },
        bottomBar = {
            BotonesPaso(onAtras = { navController.popBackStack() }) {
                if (planViewModel.validarFertilizantes()) navController.navigate("plan/dosis")
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
            IndicadorPaso(2)

            Text(
                text = "Marca los fertilizantes disponibles. El cálculo elige entre ellos.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            planViewModel.fertilizantes.forEach { fertilizante ->
                val marcado = fertilizante.id in estado.seleccionados
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (marcado) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        // Toda la tarjeta sirve para marcar/desmarcar
                        .clickable { planViewModel.onFertilizanteToggle(fertilizante.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = marcado,
                            onCheckedChange = { planViewModel.onFertilizanteToggle(fertilizante.id) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fertilizante.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = composicion(fertilizante), fontSize = 14.sp)
                        }
                        Text(
                            text = "Grupo ${fertilizante.grupo.etiqueta}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            estado.errorFertilizantes?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
