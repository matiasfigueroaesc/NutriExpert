
package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.viewmodel.RegistroViewModel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(navController: NavHostController) {

    // Obtiene el ViewModel y el estado del registro
    val registroViewModel: RegistroViewModel = viewModel()
    val estado = registroViewModel.estado

    // Estado visual: controla la ventana de privacidad
    var mostrarPolitica by remember {
        mutableStateOf(false)
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                actions = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Volver al Login",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },

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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Regístrate en NutriExpert",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Nombre
            OutlinedTextField(
                value = estado.nombre,
                onValueChange = registroViewModel::onNombreChange,
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Correo
            OutlinedTextField(
                value = estado.correo,
                onValueChange = registroViewModel::onCorreoChange,
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Contraseña
            OutlinedTextField(
                value = estado.contrasena,
                onValueChange = registroViewModel::onContrasenaChange,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Confirmación de contraseña
            OutlinedTextField(
                value = estado.confirmarContrasena,
                onValueChange = registroViewModel::onConfirmarContrasenaChange,
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Enlace para leer la política de privacidad
            TextButton(
                onClick = {
                    mostrarPolitica = true
                }
            ) {
                Text("Leer política de privacidad")
            }

            // Ventana emergente con la política de privacidad
            if (mostrarPolitica) {
                AlertDialog(
                    onDismissRequest = {
                        mostrarPolitica = false
                    },
                    title = {
                        Text("Política de privacidad")
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .height(350.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = """
                                    VERSIÓN DE DEMOSTRACIÓN

                                    NutriExpert es una aplicación académica para apoyar la gestión agrícola y la planificación del fertirriego.

                                    1. INFORMACIÓN SOLICITADA

                                    Para crear una cuenta se solicitará nombre, correo electrónico y contraseña.

                                    La aplicación también podrá gestionar información de predios, cultivos y actividades agrícolas.

                                    2. USO DE LA INFORMACIÓN

                                    Los datos se utilizarán para identificar al usuario y organizar la información registrada en la aplicación.

                                    3. PROTECCIÓN DE DATOS

                                    El proyecto contempla implementar autenticación y separación de los datos entre usuarios. Estas funciones aún están en desarrollo.

                                    4. FUNCIONAMIENTO SIN CONEXIÓN

                                    Se contempla que los usuarios previamente autenticados puedan utilizar determinadas funciones sin internet mediante almacenamiento local.

                                    5. ALCANCE

                                    Esta política es ficticia y se utiliza únicamente para demostrar el prototipo académico. No es una política definitiva ni confirma que todas las funciones estén implementadas.

                                    Última actualización: octubre de 2026.
                                """.trimIndent()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mostrarPolitica = false
                            }
                        ) {
                            Text("Cerrar")
                        }
                    }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = estado.aceptaPrivacidad,
                    onCheckedChange =
                        registroViewModel::onAceptaPrivacidadChange
                )

                Text(
                    text = "Acepto la política de privacidad",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Muestra errores de validación
            if (estado.mensajeError.isNotEmpty()) {
                Text(
                    text = estado.mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Muestra el resultado cuando el formulario es válido
            if (estado.mensajeInformativo.isNotEmpty()) {
                Text(
                    text = estado.mensajeInformativo,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Botón para validar el formulario
            Button(
                onClick = {
                    registroViewModel.registrar()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Crear cuenta")
            }
        }
    }
}
