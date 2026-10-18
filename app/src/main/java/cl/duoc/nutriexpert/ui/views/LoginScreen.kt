
package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.R
import cl.duoc.nutriexpert.ui.components.CampoFormulario
import cl.duoc.nutriexpert.viewmodel.LoginViewModel

@Composable
fun LoginScreen(navController: NavHostController) {

    // Obtiene el ViewModel y el estado del formulario
    val loginViewModel: LoginViewModel = viewModel()
    val estado = loginViewModel.estado

    // Navega cuando el ViewModel confirma el ingreso
    LaunchedEffect(estado.ingresoCorrecto) {
        if (estado.ingresoCorrecto) {
            navController.navigate("predios") {
                popUpTo("login") {
                    inclusive = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Logo: hoja blanca dentro de un cuadrado verde redondeado
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_hoja),
                contentDescription = "Logo NutriExpert",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "NutriExpert",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Fertirriego & Programación",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de correo
        CampoFormulario(
            valor = estado.correo,
            onValorChange = loginViewModel::onCorreoChange,
            etiqueta = "Correo",
            error = estado.errores.correo,
            tipoTeclado = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo de contraseña
        OutlinedTextField(
            value = estado.contrasena,
            onValueChange = loginViewModel::onContrasenaChange,
            label = { Text("Contraseña") },
            singleLine = true,
            isError = estado.errores.contrasena != null,
            supportingText = {
                estado.errores.contrasena?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Mensaje general: credenciales de prueba incorrectas
        // Pendiente: conectar autenticación real con Spring Boot y Retrofit
        if (estado.mensajeError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = estado.mensajeError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        TextButton(
            onClick = {
                navController.navigate("recuperar")
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                "¿Olvidaste tu contraseña?",
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de ingresar
        Button(
            onClick = {
                loginViewModel.ingresar()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Ingresar", fontSize = 16.sp)
        }

        // Acceso de demostración: todavía no se verifican cuentas reales
        Text(
            text = "ACCESO DE PRUEBA\nCorreo: prueba@gmail.com\nContraseña: 12345678",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                navController.navigate("registro")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Crear cuenta", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                /* Tutorial: pendiente */
            }
        ) {
            Text("Ver tutorial")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Después del primer ingreso, la app funciona sin conexión.",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(
                alpha = 0.7f
            )
        )
    }
}
