
package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.viewmodel.RecuperarContrasenaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarScreen(navController: NavHostController) {

    // Obtiene el ViewModel y el estado del formulario
    val recuperarViewModel: RecuperarContrasenaViewModel = viewModel()
    val estado = recuperarViewModel.estado

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Ingresa tu correo electrónico para recuperar tu contraseña.",
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de correo
            OutlinedTextField(
                value = estado.correo,
                onValueChange = recuperarViewModel::onCorreoChange,
                label = { Text("Correo electrónico") },
                singleLine = true,
                isError = estado.mensajeError.isNotEmpty(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Botón para solicitar recuperación
            Button(
                onClick = {
                    recuperarViewModel.solicitarRecuperacion()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Solicitar recuperación")
            }

            // Muestra errores de validación
            if (estado.mensajeError.isNotEmpty()) {
                Text(
                    text = estado.mensajeError,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Muestra el mensaje informativo
            if (estado.mensajeInformativo.isNotEmpty()) {
                Text(
                    text = estado.mensajeInformativo,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Regresa a la pantalla anterior
            Button(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text("Volver")
            }
        }
    }
}
