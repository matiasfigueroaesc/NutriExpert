
package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.nutriexpert.data.local.AnalisisEntity
import cl.duoc.nutriexpert.utils.ErroresAnalisis
import cl.duoc.nutriexpert.utils.validarAnalisis

// Formulario del análisis de suelo y agua. Usa la misma barra verde que "Nuevo predio".
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearAnalisisScreen(
    sectorId: Int,
    onGuardar: (AnalisisEntity) -> Unit,
    onVolver: () -> Unit
) {
    var fecha by remember { mutableStateOf("") }
    var profundidad by remember { mutableStateOf("30") }
    var densidad by remember { mutableStateOf("1.3") }
    var pedregosidad by remember { mutableStateOf("0") }

    var sueloN by remember { mutableStateOf("") }
    var sueloP by remember { mutableStateOf("") }
    var sueloK by remember { mutableStateOf("") }
    var sueloCa by remember { mutableStateOf("") }
    var sueloMg by remember { mutableStateOf("") }

    var aguaN by remember { mutableStateOf("") }
    var aguaP by remember { mutableStateOf("") }
    var aguaK by remember { mutableStateOf("") }
    var aguaCa by remember { mutableStateOf("") }
    var aguaMg by remember { mutableStateOf("") }
    var aguaM3Ha by remember { mutableStateOf("") }

    var errores by remember {
        mutableStateOf(ErroresAnalisis())
    }

    val valores = mapOf(
        "profundidadCm" to profundidad,
        "densidadAparente" to densidad,
        "pedregosidadPct" to pedregosidad,
        "sueloN" to sueloN,
        "sueloP" to sueloP,
        "sueloK" to sueloK,
        "sueloCa" to sueloCa,
        "sueloMg" to sueloMg,
        "aguaN" to aguaN,
        "aguaP" to aguaP,
        "aguaK" to aguaK,
        "aguaCa" to aguaCa,
        "aguaMg" to aguaMg,
        "aguaM3Ha" to aguaM3Ha
    )

    fun convertir(valor: String): Double =
        valor.trim().replace(",", ".").toDouble()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo análisis") },
                navigationIcon = {
                    // Flecha atrás en vez del botón "Volver" de abajo
                    IconButton(onClick = onVolver) {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = fecha,
                onValueChange = {
                    fecha = it
                    errores = errores.copy(fecha = null)
                },
                label = { Text("Fecha (AAAA-MM-DD)") },
                isError = errores.fecha != null,
                supportingText = {
                    errores.fecha?.let { Text(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                "Características del suelo",
                style = MaterialTheme.typography.titleMedium
            )

            CampoAnalisis(
                "Profundidad (cm)", profundidad,
                errores.campos["profundidadCm"]
            ) {
                profundidad = it
            }

            CampoAnalisis(
                "Densidad aparente", densidad,
                errores.campos["densidadAparente"]
            ) {
                densidad = it
            }

            CampoAnalisis(
                "Pedregosidad (%)", pedregosidad,
                errores.campos["pedregosidadPct"]
            ) {
                pedregosidad = it
            }

            Text(
                "Nutrientes del suelo (ppm)",
                style = MaterialTheme.typography.titleMedium
            )

            CampoAnalisis("Nitrógeno (N)", sueloN, errores.campos["sueloN"]) {
                sueloN = it
            }
            CampoAnalisis("Fósforo (P)", sueloP, errores.campos["sueloP"]) {
                sueloP = it
            }
            CampoAnalisis("Potasio (K)", sueloK, errores.campos["sueloK"]) {
                sueloK = it
            }
            CampoAnalisis("Calcio (Ca)", sueloCa, errores.campos["sueloCa"]) {
                sueloCa = it
            }
            CampoAnalisis("Magnesio (Mg)", sueloMg, errores.campos["sueloMg"]) {
                sueloMg = it
            }

            Text(
                "Nutrientes del agua (mg/L)",
                style = MaterialTheme.typography.titleMedium
            )

            CampoAnalisis("Nitrógeno (N)", aguaN, errores.campos["aguaN"]) {
                aguaN = it
            }
            CampoAnalisis("Fósforo (P)", aguaP, errores.campos["aguaP"]) {
                aguaP = it
            }
            CampoAnalisis("Potasio (K)", aguaK, errores.campos["aguaK"]) {
                aguaK = it
            }
            CampoAnalisis("Calcio (Ca)", aguaCa, errores.campos["aguaCa"]) {
                aguaCa = it
            }
            CampoAnalisis("Magnesio (Mg)", aguaMg, errores.campos["aguaMg"]) {
                aguaMg = it
            }
            CampoAnalisis("Agua aplicada (m³/ha)", aguaM3Ha, errores.campos["aguaM3Ha"]) {
                aguaM3Ha = it
            }

            Button(
                onClick = {
                    val resultado = validarAnalisis(
                        fecha = fecha,
                        valores = valores
                    )

                    errores = resultado

                    if (!resultado.hayErrores()) {
                        onGuardar(
                            AnalisisEntity(
                                sectorId = sectorId,
                                fecha = fecha,
                                profundidadCm = convertir(profundidad),
                                densidadAparente = convertir(densidad),
                                pedregosidadPct = convertir(pedregosidad),
                                sueloN = convertir(sueloN),
                                sueloP = convertir(sueloP),
                                sueloK = convertir(sueloK),
                                sueloCa = convertir(sueloCa),
                                sueloMg = convertir(sueloMg),
                                aguaN = convertir(aguaN),
                                aguaP = convertir(aguaP),
                                aguaK = convertir(aguaK),
                                aguaCa = convertir(aguaCa),
                                aguaMg = convertir(aguaMg),
                                aguaM3Ha = convertir(aguaM3Ha)
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar análisis")
            }
        }
    }
}

@Composable
private fun CampoAnalisis(
    etiqueta: String,
    valor: String,
    error: String?,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = {
            error?.let { Text(it) }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        )
    )
}
