
package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.ui.components.BarraSuperior
import cl.duoc.nutriexpert.ui.components.BotonPrincipal
import cl.duoc.nutriexpert.ui.components.CampoFormulario
import cl.duoc.nutriexpert.viewmodel.RecuperarContrasenaViewModel

@Composable
fun RecuperarScreen(navController: NavHostController) {

    // Obtiene el ViewModel y el estado del formulario
    val recuperarViewModel: RecuperarContrasenaViewModel = viewModel()
    val estado = recuperarViewModel.estado

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Recuperar contraseña",
                onVolver = { navController.popBackStack() }
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

            Text(
                text = "Ingresa tu correo electrónico para recuperar tu contraseña.",
                color = MaterialTheme.colorScheme.onBackground
            )

            // Campo de correo con validación visual (el error usa "" para "sin error")
            CampoFormulario(
                valor = estado.correo,
                onValorChange = recuperarViewModel::onCorreoChange,
                etiqueta = "Correo electrónico",
                error = estado.mensajeError.ifEmpty { null },
                icono = Icons.Filled.Email,
                tipoTeclado = KeyboardType.Email
            )

            BotonPrincipal(
                texto = "Solicitar recuperación",
                onClick = { recuperarViewModel.solicitarRecuperacion() }
            )

            // Mensaje informativo cuando el correo es válido
            if (estado.mensajeInformativo.isNotEmpty()) {
                Text(
                    text = estado.mensajeInformativo,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
