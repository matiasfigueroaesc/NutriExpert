
package cl.duoc.nutriexpert.ui.views

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.data.local.PredioEntity
import cl.duoc.nutriexpert.data.repository.ClimaRepository
import cl.duoc.nutriexpert.data.repository.PronosticoDia
import cl.duoc.nutriexpert.nativo.FotoHelper
import cl.duoc.nutriexpert.ui.components.PendienteSincronizar
import cl.duoc.nutriexpert.ui.plan.formatoCorto
import cl.duoc.nutriexpert.viewmodel.PrediosViewModel

private val VerdeOscuro = Color(0xFF075B37)
private val VerdePrincipal = Color(0xFF216A39)
private val VerdeSuave = Color(0xFFEAF6E5)
private val FondoMarfil = Color(0xFFFAF8F3)

@Composable
fun PrediosScreen(
    navController: NavHostController,
    viewModel: PrediosViewModel = viewModel()
) {
    val predios by viewModel.predios.collectAsState()
    var predioAEliminar by remember { mutableStateOf<PredioEntity?>(null) }
    val totalHectareas = predios.sumOf { it.hectareas }

    Scaffold(
        containerColor = FondoMarfil,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .background(VerdeOscuro)
            ) {
                // Ondas verdes del diseño premium, dibujadas en alta resolución.
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF087445), Color(0xFF06452F)),
                            start = androidx.compose.ui.geometry.Offset.Zero,
                            end = androidx.compose.ui.geometry.Offset(w, h)
                        )
                    )
                    val ondaSuperior = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        cubicTo(w * .78f, h * .38f, w * .65f, h * .03f, w * .43f, h * .25f)
                        cubicTo(w * .22f, h * .47f, w * .15f, h * .12f, 0f, h * .10f)
                        close()
                    }
                    drawPath(ondaSuperior, Color(0xFF1A8B56).copy(alpha = .38f))
                    val ondaDerecha = Path().apply {
                        moveTo(w, h * .12f)
                        cubicTo(w * .78f, h * .18f, w * .76f, h * .53f, w * .56f, h * .60f)
                        cubicTo(w * .76f, h * .48f, w * .88f, h * .75f, w, h * .65f)
                        close()
                    }
                    drawPath(ondaDerecha, Color(0xFF38A36A).copy(alpha = .22f))
                    val ondaInferior = Path().apply {
                        moveTo(0f, h * .72f)
                        cubicTo(w * .27f, h * .47f, w * .41f, h * .95f, w * .69f, h * .74f)
                        cubicTo(w * .86f, h * .65f, w * .94f, h * .88f, w, h * .80f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(ondaInferior, Color(0xFF06462E).copy(alpha = .42f))
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // El PNG ya incluye la N, la línea vertical y NutriExpert.
                    // Se muestra completo, sin recortes ni textos duplicados.
                    Image(
                        painter = painterResource(
                            id = cl.duoc.nutriexpert.R.drawable.logo_nutriexpert_premium
                        ),
                        contentDescription = "Logo NutriExpert",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(150.dp)
                            .height(42.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(7.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .align(Alignment.CenterHorizontally)
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.35f))
                    )
                    Spacer(Modifier.height(9.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            navController.navigate("inicio") {
                                popUpTo("inicio") { inclusive = false }
                                launchSingleTop = true
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver a Inicio", tint = Color.White)
                        }
                        Spacer(Modifier.width(5.dp))
                        Text("Mis predios", fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 6.dp) {
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate("inicio") {
                            popUpTo("inicio") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Home, "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Filled.LocationOn, "Mis predios") },
                    label = { Text("Mis predios") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = VerdeOscuro,
                        selectedTextColor = VerdeOscuro,
                        indicatorColor = VerdeSuave
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {}, // Perfil sigue siendo un marcador de posición.
                    icon = { Icon(Icons.Filled.Person, "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("predio_form/0") },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Nuevo predio", fontWeight = FontWeight.SemiBold) },
                containerColor = VerdePrincipal,
                contentColor = Color.White,
                shape = RoundedCornerShape(22.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "${predios.size} predio(s) · ${formatoCorto(totalHectareas)} ha",
                    fontSize = 17.sp,
                    color = VerdeOscuro,
                    fontWeight = FontWeight.Medium
                )
            }
            if (predios.isEmpty()) {
                item { Text("Aún no tienes predios. Toca Nuevo predio para agregar el primero.") }
            }
            items(predios, key = { it.id }) { predio ->
                PredioCard(
                    predio = predio,
                    onEditar = { navController.navigate("predio_form/${predio.id}") },
                    onEliminar = { predioAEliminar = predio },
                    onVerSectores = { navController.navigate("sectores/${predio.id}") },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    predioAEliminar?.let { predio ->
        AlertDialog(
            onDismissRequest = { predioAEliminar = null },
            title = { Text("Eliminar predio") },
            text = { Text("¿Seguro que quieres eliminar \"${predio.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(predio)
                    predioAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { predioAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun PredioCard(
    predio: PredioEntity,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onVerSectores: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandida by remember { mutableStateOf(false) }
    val giroFlecha by animateFloatAsState(if (expandida) 180f else 0f, label = "giroFlecha")
    val context = LocalContext.current
    val miniatura by produceState<ImageBitmap?>(initialValue = null, predio.fotoUri) {
        value = predio.fotoUri?.let { FotoHelper.cargarVistaPrevia(context, it, maxLado = 200) }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(Modifier.padding(12.dp).animateContentSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expandida = !expandida }.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(66.dp).clip(RoundedCornerShape(17.dp)).background(VerdeSuave),
                    contentAlignment = Alignment.Center
                ) {
                    if (miniatura != null) {
                        Image(
                            bitmap = miniatura!!,
                            contentDescription = "Foto de ${predio.nombre}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Filled.LocationOn, "Predio sin fotografía", tint = VerdePrincipal, modifier = Modifier.size(30.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(predio.nombre, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A251C))
                    Text(predio.comuna, fontSize = 14.sp, color = Color.DarkGray)
                    // Ícono cuando el servidor todavía no tiene este predio
                    if (!predio.sincronizado) {
                        PendienteSincronizar()
                    }
                }
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(VerdeSuave).padding(horizontal = 9.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${formatoCorto(predio.hectareas)} ha", color = VerdeOscuro, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(2.dp))
                    Icon(Icons.Filled.KeyboardArrowDown, if (expandida) "Contraer" else "Desplegar", tint = VerdeOscuro, modifier = Modifier.rotate(giroFlecha))
                }
            }

            if (expandida) {
                Spacer(Modifier.height(12.dp))
                if (predio.latitud != null && predio.longitud != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, tint = VerdePrincipal, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Ubicación: %.5f, %.5f".format(predio.latitud, predio.longitud),
                            fontSize = 13.sp, color = Color.DarkGray
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(VerdeSuave).padding(14.dp)
                ) {
                    PronosticoPredio7Dias(predio)
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onVerSectores,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF138A36), Color(0xFF08783B), Color(0xFF005C37))
                                ), RoundedCornerShape(24.dp)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) { Text("Ver sectores", fontWeight = FontWeight.Bold) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        TextButton(onClick = onEditar) {
                            Icon(Icons.Filled.Edit, null, tint = VerdePrincipal, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Editar", color = VerdePrincipal)
                        }
                        TextButton(onClick = onEliminar) {
                            Icon(Icons.Filled.Delete, null, tint = Color(0xFFB42318), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Eliminar", color = Color(0xFFB42318))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PronosticoPredio7Dias(predio: PredioEntity) {
    val latitud = predio.latitud
    val longitud = predio.longitud
    if (latitud == null || longitud == null) {
        Text("Agrega la ubicación GPS para ver el pronóstico.")
        return
    }
    val pronostico by produceState<List<PronosticoDia>?>(initialValue = null, latitud, longitud) {
        value = ClimaRepository().obtenerPronostico7Dias(latitud, longitud)
    }
    Text("Pronóstico de 7 días", fontWeight = FontWeight.Bold, color = VerdeOscuro, fontSize = 17.sp)
    Spacer(Modifier.height(8.dp))
    when {
        pronostico == null -> Text("Cargando pronóstico...")
        pronostico!!.isEmpty() -> Text("No se pudo obtener el pronóstico.")
        else -> pronostico!!.forEach { dia ->
            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(dia.fecha, fontSize = 13.sp)
                Text("${dia.temperaturaMinima.toInt()}° / ${dia.temperaturaMaxima.toInt()}°", fontSize = 13.sp)
            }
        }
    }
}
