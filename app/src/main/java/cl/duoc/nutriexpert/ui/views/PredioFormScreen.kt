package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.ui.components.CampoFormulario
import cl.duoc.nutriexpert.viewmodel.PredioFormViewModel
import cl.duoc.nutriexpert.ui.components.FotoPredioSection
import cl.duoc.nutriexpert.ui.components.UbicacionPredioSection

// Formulario para crear (predioId = 0) o editar un predio (RF-09).
// La pantalla solo dibuja el estado del ViewModel y le avisa lo que hace el usuario.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredioFormScreen(
    navController: NavHostController,
    predioId: Int,
    viewModel: PredioFormViewModel = viewModel()
) {
    val estado = viewModel.estado

    // Al abrir la pantalla en modo edición, carga los datos del predio
    LaunchedEffect(predioId) {
        viewModel.cargar(predioId)
    }

    // Cuando el ViewModel avisa que se guardó, volvemos a la lista
    LaunchedEffect(estado.guardado) {
        if (estado.guardado) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (predioId == 0) "Nuevo predio" else "Editar predio") },
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
                valor = estado.nombre,
                onValorChange = { viewModel.onNombreChange(it) },
                etiqueta = "Nombre del predio",
                error = estado.errores.nombre,
                icono = Icons.Filled.Home
            )

            CampoFormulario(
                valor = estado.comuna,
                onValorChange = { viewModel.onComunaChange(it) },
                etiqueta = "Comuna",
                error = estado.errores.comuna,
                icono = Icons.Filled.Place
            )

            CampoFormulario(
                valor = estado.hectareas,
                onValorChange = { viewModel.onHectareasChange(it) },
                etiqueta = "Superficie (ha)",
                error = estado.errores.hectareas,
                tipoTeclado = KeyboardType.Decimal
            )

            // Recursos nativos: aquí van los botones de GPS y cámara (parte de Fabián).
            // Para guardar usen viewModel.onUbicacionChange(lat, lon) y viewModel.onFotoChange(uri).
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Recursos nativos: GPS y cámara/galería
                UbicacionPredioSection(
                    latitud = estado.latitud,
                    longitud = estado.longitud,
                    onUbicacionObtenida = { lat, lon -> viewModel.onUbicacionChange(lat, lon) }
                )

                FotoPredioSection(
                    fotoUri = estado.fotoUri,
                    onFotoSeleccionada = { viewModel.onFotoChange(it) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.guardar() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar predio", fontSize = 16.sp)
            }

            Text(
                text = "Se guarda en el teléfono, funciona sin conexión.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
