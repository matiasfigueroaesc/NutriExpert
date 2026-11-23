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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.ui.components.BotonPrincipal
import cl.duoc.nutriexpert.ui.components.BotonSecundario
import cl.duoc.nutriexpert.ui.components.CampoFormulario
import cl.duoc.nutriexpert.viewmodel.PerfilViewModel

private val PerfilVerde = Color(0xFF17542F)
private val PerfilMarfil = Color(0xFFFFFBF4)

// Mi perfil: datos del usuario, cambiar el nombre, cerrar sesión y borrar la cuenta.
// Cambiar el nombre y borrar la cuenta pasan por el microservicio usuarios (necesitan internet).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavHostController) {

    val viewModel: PerfilViewModel = viewModel()
    val estado = viewModel.estado

    var confirmarBorrado by remember { mutableStateOf(false) }

    // Al cerrar sesión o borrar la cuenta: al login, sin dejar pantallas atrás
    LaunchedEffect(estado.salir) {
        if (estado.salir) {
            navController.navigate("login") {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = PerfilMarfil,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi perfil",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PerfilVerde)
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

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFE6EED7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = PerfilVerde,
                    modifier = Modifier.size(56.dp)
                )
            }

            Text(
                text = estado.nombre.ifBlank { "Mi perfil" },
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = PerfilVerde
            )

            Text(
                text = estado.correo,
                fontSize = 15.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(8.dp))

            // Cambiar el nombre
            CampoFormulario(
                valor = estado.nombreEditado,
                onValorChange = viewModel::onNombreChange,
                etiqueta = "Nombre",
                error = estado.errorNombre,
                icono = Icons.Filled.Person
            )

            BotonPrincipal(
                texto = if (estado.guardando) "Guardando…" else "Guardar nombre",
                onClick = { viewModel.guardarNombre() },
                icono = Icons.Filled.Check,
                cargando = estado.guardando
            )

            if (estado.mensaje.isNotEmpty()) {
                Text(text = estado.mensaje, color = PerfilVerde)
            }
            if (estado.mensajeError.isNotEmpty()) {
                Text(text = estado.mensajeError, color = MaterialTheme.colorScheme.error)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            BotonSecundario(
                texto = "Cerrar sesión",
                onClick = { viewModel.cerrarSesion() },
                icono = Icons.AutoMirrored.Filled.ExitToApp
            )

            TextButton(
                onClick = { confirmarBorrado = true },
                enabled = !estado.borrando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (estado.borrando) "  Borrando cuenta…" else "  Borrar mi cuenta",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Borrar la cuenta no se puede deshacer: se pide confirmación
    if (confirmarBorrado) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("¿Borrar tu cuenta?") },
            text = {
                Text("Se borrará tu cuenta del servidor y de este teléfono. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarBorrado = false
                    viewModel.borrarCuenta()
                }) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
