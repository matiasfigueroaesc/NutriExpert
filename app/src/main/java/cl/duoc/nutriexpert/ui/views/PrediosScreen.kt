package cl.duoc.nutriexpert.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.data.local.PredioEntity
import cl.duoc.nutriexpert.nativo.FotoHelper
import cl.duoc.nutriexpert.viewmodel.PrediosViewModel

// Pantalla "Mis predios" (RF-09): lista los predios guardados en Room.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrediosScreen(
    navController: NavHostController,
    viewModel: PrediosViewModel = viewModel()
) {
    // La lista viene del ViewModel y se actualiza sola al guardar o borrar
    val predios by viewModel.predios.collectAsState()
    // Predio que el usuario quiere borrar (null = no se muestra el diálogo)
    var predioAEliminar by remember { mutableStateOf<PredioEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis predios") },
                actions = {
                    // El Login se sacó de la pila al ingresar, así que volvemos navegando a "login"
                    IconButton(
                        onClick = {
                            navController.navigate("login") {
                                popUpTo("predios") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("predio_form/0") },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo predio") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Resumen: cantidad de predios y superficie total
            val totalHectareas = predios.sumOf { it.hectareas }
            Text(
                text = "${predios.size} predio(s) · ${"%.1f".format(totalHectareas)} ha",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            // Entrada al asistente del plan nutricional (4 pasos)
            OutlinedButton(
                onClick = { navController.navigate("plan/cultivo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Nuevo plan nutricional")
            }

            // Mensaje cuando todavía no hay predios (aparece con animación)
            AnimatedVisibility(
                visible = predios.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Aún no tienes predios. Toca \"Nuevo predio\" para agregar el primero.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
            ) {
                items(predios, key = { it.id }) { predio ->
                    PredioCard(
                        predio = predio,
                        onEditar = { navController.navigate("predio_form/${predio.id}") },
                        onEliminar = { predioAEliminar = predio },
                        // Anima la tarjeta cuando se agrega, borra o cambia de lugar
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    // Diálogo para confirmar antes de borrar
    predioAEliminar?.let { predio ->
        AlertDialog(
            onDismissRequest = { predioAEliminar = null },
            title = { Text("Eliminar predio") },
            text = { Text("¿Seguro que quieres eliminar \"${predio.nombre}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminar(predio)
                        predioAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { predioAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Tarjeta de un predio. Al tocarla se expande y muestra los botones Editar y Eliminar.
@Composable
fun PredioCard(
    predio: PredioEntity,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandida by remember { mutableStateOf(false) }
    // La flecha gira suavemente al abrir y cerrar la tarjeta
    val giroFlecha by animateFloatAsState(
        targetValue = if (expandida) 180f else 0f,
        label = "giroFlecha"
    )
    // Carga la foto reducida en segundo plano (FotoHelper, parte de recursos nativos)
    val context = LocalContext.current
    val miniatura by produceState<ImageBitmap?>(initialValue = null, predio.fotoUri) {
        value = predio.fotoUri?.let { FotoHelper.cargarVistaPrevia(context, it, maxLado = 200) }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier
            .fillMaxWidth()
            .clickable { expandida = !expandida }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()   // la tarjeta crece y se achica con animación
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Miniatura de la foto del predio, si tiene
                miniatura?.let { imagen ->
                    Image(
                        bitmap = imagen,
                        contentDescription = "Foto de ${predio.nombre}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = predio.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = predio.comuna,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "${"%.1f".format(predio.hectareas)} ha",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expandida) "Cerrar" else "Ver más",
                    modifier = Modifier.rotate(giroFlecha)
                )
            }

            if (expandida) {
                Spacer(modifier = Modifier.height(8.dp))
                if (predio.latitud != null && predio.longitud != null) {
                    Text(
                        text = "Ubicación: %.5f, %.5f".format(predio.latitud, predio.longitud),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onEditar) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }
                    TextButton(onClick = onEliminar) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
