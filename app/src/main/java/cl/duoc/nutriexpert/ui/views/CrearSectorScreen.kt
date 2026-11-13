
package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.nutriexpert.data.local.SectorEntity
import cl.duoc.nutriexpert.utils.ErroresSector
import cl.duoc.nutriexpert.utils.validarSector

// Formulario para crear un sector. Usa la misma barra verde que "Nuevo predio".
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSectorScreen(
    predioId: Int,
    onGuardar: (SectorEntity) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var hectareas by remember { mutableStateOf("") }
    var cultivoId by remember { mutableStateOf("") }
    var menuExpandido by remember { mutableStateOf(false) }
    var errores by remember { mutableStateOf(ErroresSector()) }

    val cultivos = listOf(
        "cultivo-tomate" to "Tomate",
        "cultivo-lechuga" to "Lechuga"
    )

    val nombreCultivo = cultivos
        .firstOrNull { it.first == cultivoId }
        ?.second.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo sector") },
                navigationIcon = {
                    // Flecha atrás en vez del botón "Volver" de abajo
                    IconButton(onClick = onVolver) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    errores = errores.copy(nombre = null)
                },
                label = { Text("Nombre del sector") },
                isError = errores.nombre != null,
                supportingText = {
                    errores.nombre?.let { Text(it) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = menuExpandido,
                onExpandedChange = { menuExpandido = it }
            ) {
                OutlinedTextField(
                    value = nombreCultivo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cultivo") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = menuExpandido
                        )
                    },
                    isError = errores.cultivo != null,
                    supportingText = {
                        errores.cultivo?.let { Text(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false }
                ) {
                    cultivos.forEach { (id, nombreOpcion) ->
                        DropdownMenuItem(
                            text = { Text(nombreOpcion) },
                            onClick = {
                                cultivoId = id
                                menuExpandido = false
                                errores = errores.copy(cultivo = null)
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = hectareas,
                onValueChange = {
                    hectareas = it
                    errores = errores.copy(hectareas = null)
                },
                label = { Text("Superficie (hectáreas)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError = errores.hectareas != null,
                supportingText = {
                    errores.hectareas?.let { Text(it) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val resultado = validarSector(
                        nombre = nombre,
                        cultivoId = cultivoId,
                        hectareas = hectareas
                    )

                    errores = resultado

                    if (!resultado.hayErrores()) {
                        val superficie = hectareas
                            .trim()
                            .replace(",", ".")
                            .toDouble()

                        onGuardar(
                            SectorEntity(
                                predioId = predioId,
                                nombre = nombre.trim(),
                                cultivoId = cultivoId,
                                hectareas = superficie
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar sector")
            }
        }
    }
}
