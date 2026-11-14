
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.ui.components.BarraSuperior
import cl.duoc.nutriexpert.ui.components.BotonPrincipal
import cl.duoc.nutriexpert.ui.components.CampoFormulario
import cl.duoc.nutriexpert.viewmodel.RegistroViewModel

@Composable
fun RegistroScreen(navController: NavHostController) {

    // Obtiene el ViewModel y el estado del registro
    val registroViewModel: RegistroViewModel = viewModel()
    val estado = registroViewModel.estado

    // Controla la ventana de privacidad
    var mostrarPolitica by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Crear cuenta",
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Regístrate en NutriExpert",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Los errores de registro usan "" para "sin error"; el campo espera null
            CampoFormulario(
                valor = estado.nombre,
                onValorChange = registroViewModel::onNombreChange,
                etiqueta = "Nombre",
                error = estado.errores.nombre.ifEmpty { null },
                icono = Icons.Filled.Person
            )

            CampoFormulario(
                valor = estado.correo,
                onValorChange = registroViewModel::onCorreoChange,
                etiqueta = "Correo electrónico",
                error = estado.errores.correo.ifEmpty { null },
                icono = Icons.Filled.Email,
                tipoTeclado = KeyboardType.Email
            )

            CampoFormulario(
                valor = estado.contrasena,
                onValorChange = registroViewModel::onContrasenaChange,
                etiqueta = "Contraseña",
                error = estado.errores.contrasena.ifEmpty { null },
                icono = Icons.Filled.Lock,
                esContrasena = true
            )

            CampoFormulario(
                valor = estado.confirmarContrasena,
                onValorChange = registroViewModel::onConfirmarContrasenaChange,
                etiqueta = "Confirmar contraseña",
                error = estado.errores.confirmarContrasena.ifEmpty { null },
                icono = Icons.Filled.Lock,
                esContrasena = true
            )

            // Aceptación de la política de privacidad
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = estado.aceptaPrivacidad,
                    onCheckedChange = registroViewModel::onAceptaPrivacidadChange
                )
                Text(
                    text = "Acepto la política de privacidad",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            // Error de aceptación de privacidad
            if (estado.errores.privacidad.isNotEmpty()) {
                Text(
                    text = estado.errores.privacidad,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Enlace para leer la política de privacidad
            TextButton(
                onClick = { mostrarPolitica = true },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("Leer política de privacidad")
            }

            // Mensaje general, si se utiliza
            if (estado.mensajeError.isNotEmpty()) {
                Text(
                    text = estado.mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Mensaje cuando el formulario es válido
            if (estado.mensajeInformativo.isNotEmpty()) {
                Text(
                    text = estado.mensajeInformativo,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            BotonPrincipal(
                texto = "Crear cuenta",
                onClick = { registroViewModel.registrar() }
            )
        }
    }

    // Ventana emergente con la política de privacidad
    if (mostrarPolitica) {
        AlertDialog(
            onDismissRequest = { mostrarPolitica = false },
            title = { Text("Política de privacidad") },
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
                TextButton(onClick = { mostrarPolitica = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
