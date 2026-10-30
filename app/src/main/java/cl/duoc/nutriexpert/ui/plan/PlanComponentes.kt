package cl.duoc.nutriexpert.ui.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.domain.model.GrupoEstanque
import cl.duoc.nutriexpert.domain.model.Nutriente
import java.util.Locale

// Total de pasos del asistente del plan
const val TOTAL_PASOS = 4

private val LOCALE_CL = Locale("es", "CL")

// Número con 2 decimales y coma decimal (1398.79 -> "1.398,79")
fun formato(valor: Double): String = String.format(LOCALE_CL, "%,.2f", valor)

// Número con 1 decimal (10.79 -> "10,8")
fun formato1(valor: Double): String = String.format(LOCALE_CL, "%,.1f", valor)

// Número sin decimales si es entero (5.0 -> "5", 1000.0 -> "1.000", 2.5 -> "2,5")
fun formatoCorto(valor: Double): String =
    if (valor % 1.0 == 0.0) String.format(LOCALE_CL, "%,d", valor.toLong())
    else formato(valor).trimEnd('0')

// Nombre del nutriente en palabras, para el agricultor (sin cambiar el enum)
fun nombreNutriente(n: Nutriente): String = when (n) {
    Nutriente.N -> "nitrógeno"
    Nutriente.P2O5 -> "fósforo"
    Nutriente.K2O -> "potasio"
    Nutriente.CaO -> "calcio"
    Nutriente.MgO -> "magnesio"
}

// Igual que nombreNutriente, pero con mayúscula inicial ("Nitrógeno")
fun nombreNutrienteTitulo(n: Nutriente): String = nombreNutriente(n).replaceFirstChar { it.uppercase() }

// En qué estanque va el fertilizante
fun textoEstanque(grupo: GrupoEstanque): String = when (grupo) {
    GrupoEstanque.A -> "Estanque A"
    GrupoEstanque.B -> "Estanque B"
    GrupoEstanque.AMBOS -> "Estanque A o B"
}

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

// Indicador "Paso X de 4" con barra de progreso y la pregunta del paso
@Composable
fun IndicadorPaso(paso: Int, pregunta: String) {
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
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = pregunta,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp
        )
    }
}

// Sección "Ajustes del asesor": cerrada por defecto, se abre con el botón
@Composable
fun SeccionAjustes(
    abierta: Boolean,
    onAbiertaChange: (Boolean) -> Unit,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextButton(
            onClick = { onAbiertaChange(!abierta) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Ajustes del asesor", modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (abierta) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (abierta) "Cerrar ajustes" else "Abrir ajustes"
                )
            }
        }
        AnimatedVisibility(visible = abierta) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = contenido
            )
        }
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
