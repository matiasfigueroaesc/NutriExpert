package cl.duoc.nutriexpert.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.nutriexpert.ui.components.FotoPredioSection
import cl.duoc.nutriexpert.ui.components.UbicacionPredioSection

/**
 * Pantalla de prueba de Fabián para GPS y cámara, separada del formulario
 * de predio para no chocar con el código de los demás.
 * Cuando las secciones estén conectadas al formulario, se puede borrar.
 */
@Composable
fun PruebaRecursosNativosScreen(modifier: Modifier = Modifier) {
    var latitud by rememberSaveable { mutableStateOf<Double?>(null) }
    var longitud by rememberSaveable { mutableStateOf<Double?>(null) }
    var fotoUri by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Prueba de recursos nativos", style = MaterialTheme.typography.headlineSmall)

        UbicacionPredioSection(
            latitud = latitud,
            longitud = longitud,
            onUbicacionObtenida = { lat, lng ->
                latitud = lat
                longitud = lng
            }
        )

        FotoPredioSection(
            fotoUri = fotoUri,
            onFotoSeleccionada = { fotoUri = it }
        )
    }
}
