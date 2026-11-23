
package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.nutriexpert.data.local.AnalisisEntity
import cl.duoc.nutriexpert.ui.components.BarraSuperior
import cl.duoc.nutriexpert.ui.components.BotonPrincipal
import cl.duoc.nutriexpert.ui.components.CampoFormulario
import cl.duoc.nutriexpert.utils.ErroresAnalisis
import cl.duoc.nutriexpert.utils.validarAnalisis

// Formulario "Nuevo análisis de suelo y agua". Mismo estilo que el formulario de predio.
@Composable
fun CrearAnalisisScreen(
    sectorId: Int,
    onGuardar: (AnalisisEntity) -> Unit,
    onVolver: () -> Unit
) {
    // rememberSaveable: lo escrito no se pierde al girar el teléfono
    var fecha by rememberSaveable { mutableStateOf("") }
    var profundidad by rememberSaveable { mutableStateOf("30") }
    var densidad by rememberSaveable { mutableStateOf("1.3") }
    var pedregosidad by rememberSaveable { mutableStateOf("0") }

    var sueloN by rememberSaveable { mutableStateOf("") }
    var sueloP by rememberSaveable { mutableStateOf("") }
    var sueloK by rememberSaveable { mutableStateOf("") }
    var sueloCa by rememberSaveable { mutableStateOf("") }
    var sueloMg by rememberSaveable { mutableStateOf("") }

    var aguaN by rememberSaveable { mutableStateOf("") }
    var aguaP by rememberSaveable { mutableStateOf("") }
    var aguaK by rememberSaveable { mutableStateOf("") }
    var aguaCa by rememberSaveable { mutableStateOf("") }
    var aguaMg by rememberSaveable { mutableStateOf("") }
    var aguaM3Ha by rememberSaveable { mutableStateOf("") }

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

    // Al escribir en un campo se borra su error
    fun limpiarError(clave: String) {
        errores = errores.copy(campos = errores.campos - clave)
    }

    fun convertir(valor: String): Double =
        valor.trim().replace(",", ".").toDouble()

    Scaffold(
        topBar = { BarraSuperior(titulo = "Nuevo análisis", onVolver = onVolver) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoFormulario(
                valor = fecha,
                onValorChange = {
                    fecha = it
                    errores = errores.copy(fecha = null)
                },
                etiqueta = "Fecha (AAAA-MM-DD)",
                error = errores.fecha,
                icono = Icons.Filled.DateRange
            )

            TituloSeccion("Características del suelo")

            CampoAnalisis(
                "Profundidad (cm)", profundidad,
                errores.campos["profundidadCm"]
            ) {
                profundidad = it
                limpiarError("profundidadCm")
            }

            CampoAnalisis(
                "Densidad aparente (g/cm³)", densidad,
                errores.campos["densidadAparente"]
            ) {
                densidad = it
                limpiarError("densidadAparente")
            }

            CampoAnalisis(
                "Pedregosidad (%)", pedregosidad,
                errores.campos["pedregosidadPct"]
            ) {
                pedregosidad = it
                limpiarError("pedregosidadPct")
            }

            TituloSeccion("Nutrientes del suelo (ppm)")

            CampoAnalisis("Nitrógeno (N)", sueloN, errores.campos["sueloN"]) {
                sueloN = it
                limpiarError("sueloN")
            }
            CampoAnalisis("Fósforo (P)", sueloP, errores.campos["sueloP"]) {
                sueloP = it
                limpiarError("sueloP")
            }
            CampoAnalisis("Potasio (K)", sueloK, errores.campos["sueloK"]) {
                sueloK = it
                limpiarError("sueloK")
            }
            CampoAnalisis("Calcio (Ca)", sueloCa, errores.campos["sueloCa"]) {
                sueloCa = it
                limpiarError("sueloCa")
            }
            CampoAnalisis("Magnesio (Mg)", sueloMg, errores.campos["sueloMg"]) {
                sueloMg = it
                limpiarError("sueloMg")
            }

            TituloSeccion("Nutrientes del agua (mg/L)")

            CampoAnalisis("Nitrógeno (N)", aguaN, errores.campos["aguaN"]) {
                aguaN = it
                limpiarError("aguaN")
            }
            CampoAnalisis("Fósforo (P)", aguaP, errores.campos["aguaP"]) {
                aguaP = it
                limpiarError("aguaP")
            }
            CampoAnalisis("Potasio (K)", aguaK, errores.campos["aguaK"]) {
                aguaK = it
                limpiarError("aguaK")
            }
            CampoAnalisis("Calcio (Ca)", aguaCa, errores.campos["aguaCa"]) {
                aguaCa = it
                limpiarError("aguaCa")
            }
            CampoAnalisis("Magnesio (Mg)", aguaMg, errores.campos["aguaMg"]) {
                aguaMg = it
                limpiarError("aguaMg")
            }
            CampoAnalisis("Agua aplicada (m³/ha)", aguaM3Ha, errores.campos["aguaM3Ha"]) {
                aguaM3Ha = it
                limpiarError("aguaM3Ha")
            }

            Spacer(modifier = Modifier.height(8.dp))

            BotonPrincipal(
                texto = "Guardar análisis",
                icono = Icons.Filled.Check,
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
                                fecha = fecha.trim(),
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
                }
            )

            Text(
                text = "Se guarda en el teléfono, funciona sin conexión.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

// Subtítulo de cada grupo de campos
@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun CampoAnalisis(
    etiqueta: String,
    valor: String,
    error: String?,
    onCambio: (String) -> Unit
) {
    CampoFormulario(
        valor = valor,
        onValorChange = onCambio,
        etiqueta = etiqueta,
        error = error,
        tipoTeclado = KeyboardType.Decimal
    )
}
