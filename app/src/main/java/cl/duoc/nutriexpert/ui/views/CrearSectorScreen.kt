
package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.nutriexpert.data.local.SectorEntity
import cl.duoc.nutriexpert.ui.components.BarraSuperior
import cl.duoc.nutriexpert.ui.components.BotonPrincipal
import cl.duoc.nutriexpert.ui.components.CampoFormulario
import cl.duoc.nutriexpert.utils.ErroresSector
import cl.duoc.nutriexpert.utils.validarSector

// Formulario "Nuevo sector". Mismo estilo que el formulario de predio.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSectorScreen(
    predioId: Int,
    onGuardar: (SectorEntity) -> Unit,
    onVolver: () -> Unit
) {
    // rememberSaveable: lo escrito no se pierde al girar el teléfono
    var nombre by rememberSaveable { mutableStateOf("") }
    var hectareas by rememberSaveable { mutableStateOf("") }
    var cultivoId by rememberSaveable { mutableStateOf("") }
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
        topBar = { BarraSuperior(titulo = "Nuevo sector", onVolver = onVolver) }
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
                valor = nombre,
                onValorChange = {
                    nombre = it
                    errores = errores.copy(nombre = null)
                },
                etiqueta = "Nombre del sector",
                error = errores.nombre
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
                        if (errores.cultivo != null) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido)
                        }
                    },
                    isError = errores.cultivo != null,
                    supportingText = {
                        errores.cultivo?.let { Text(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
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

            CampoFormulario(
                valor = hectareas,
                onValorChange = {
                    hectareas = it
                    errores = errores.copy(hectareas = null)
                },
                etiqueta = "Superficie (ha)",
                error = errores.hectareas,
                tipoTeclado = KeyboardType.Decimal
            )

            Spacer(modifier = Modifier.height(8.dp))

            BotonPrincipal(
                texto = "Guardar sector",
                icono = Icons.Filled.Check,
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
                }
            )

            Text(
                text = "Se guarda en el teléfono, funciona sin conexión.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
