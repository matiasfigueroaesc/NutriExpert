package cl.duoc.nutriexpert.ui.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.util.Locale

// Total de pasos del asistente del plan
const val TOTAL_PASOS = 5

private val LOCALE_CL = Locale("es", "CL")

// Número con 2 decimales y coma decimal (1398.79 -> "1.398,79")
fun formato(valor: Double): String = String.format(LOCALE_CL, "%,.2f", valor)

// Número con 1 decimal (10.79 -> "10,8")
fun formato1(valor: Double): String = String.format(LOCALE_CL, "%,.1f", valor)

// Barra superior del asistente, con flecha para volver al paso anterior
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanTopBar(titulo: String, navController: NavHostController) {
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

// Indicador "Paso X de 5" con barra de progreso
@Composable
fun IndicadorPaso(paso: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Paso $paso de $TOTAL_PASOS",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { paso / TOTAL_PASOS.toFloat() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Botones Atrás / Siguiente al pie de cada paso
@Composable
fun BotonesPaso(
    onAtras: (() -> Unit)?,
    textoSiguiente: String = "Siguiente",
    onSiguiente: () -> Unit
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (onAtras != null) {
                OutlinedButton(
                    onClick = onAtras,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Atrás", fontSize = 16.sp)
                }
            }
            Button(
                onClick = onSiguiente,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text(textoSiguiente, fontSize = 16.sp)
            }
        }
    }
}
