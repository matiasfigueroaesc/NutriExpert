
package cl.duoc.nutriexpert.ui.views

import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.R
import cl.duoc.nutriexpert.data.local.PredioEntity
import cl.duoc.nutriexpert.data.repository.ClimaRepository
import cl.duoc.nutriexpert.data.repository.DatosClima
import cl.duoc.nutriexpert.nativo.FotoHelper
import cl.duoc.nutriexpert.nativo.UbicacionHelper
import cl.duoc.nutriexpert.ui.plan.formatoCorto
import cl.duoc.nutriexpert.viewmodel.PrediosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

private val VerdeInicio = Color(0xFF075536)
private val VerdeOscuroInicio = Color(0xFF073D24)
private val VerdeLimaInicio = Color(0xFF9ACB4B)
private val MarfilInicio = Color(0xFFFFFBF4)
private val VerdeSuaveInicio = Color(0xFFE7F2DD)

@Composable
fun InicioScreen(
    navController: NavHostController,
    viewModel: PrediosViewModel = viewModel()
) {
    val predios by viewModel.predios.collectAsState()
    val totalHectareas = predios.sumOf { it.hectareas }

    Scaffold(
        containerColor = MarfilInicio,
        bottomBar = {
            BarraInferiorInicio(
                onPredios = {
                    navController.navigate("predios")
                },
                onPerfil = {
                    navController.navigate("perfil")
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CabeceraInicio(
                    onPerfil = {
                        navController.navigate("perfil")
                    }
                )
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ClimaInicio()

                    ResumenPrediosInicio(
                        cantidad = predios.size,
                        hectareas = totalHectareas
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mis predios",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerdeOscuroInicio
                        )

                        TextButton(
                            onClick = {
                                navController.navigate("predios")
                            }
                        ) {
                            Text(
                                text = "Ver todos",
                                fontWeight = FontWeight.SemiBold,
                                color = VerdeInicio
                            )

                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = VerdeInicio,
                                modifier = Modifier.rotate(-90f)
                            )
                        }
                    }
                }
            }

            if (predios.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Aún no tienes predios registrados.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(25.dp),
                            color = VerdeOscuroInicio,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(
                    items = predios.take(3),
                    key = { it.id }
                ) { predio ->
                    PredioInicioCard(
                        predio = predio,
                        onClick = {
                            navController.navigate("predios")
                        }
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        navController.navigate("predio_form/0")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdeInicio
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF168B30),
                                        VerdeInicio
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = Color.White
                            )

                            Spacer(Modifier.width(9.dp))

                            Text(
                                text = "Nuevo predio",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CabeceraInicio(
    onPerfil: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(195.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        VerdeOscuroInicio,
                        VerdeInicio
                    )
                )
            )
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val w = size.width
            val h = size.height

            val forma = Path().apply {
                moveTo(w * 0.55f, 0f)
                cubicTo(
                    w * 0.95f, h * 0.08f,
                    w * 0.65f, h * 0.65f,
                    w, h * 0.90f
                )
                lineTo(w, 0f)
                close()
            }

            drawPath(
                path = forma,
                color = Color.White.copy(alpha = 0.035f)
            )

            val formaInferior = Path().apply {
                moveTo(0f, h * 0.82f)
                cubicTo(
                    w * 0.30f, h * 0.60f,
                    w * 0.55f, h * 1.10f,
                    w, h * 0.77f
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }

            drawPath(
                path = formaInferior,
                color = VerdeLimaInicio.copy(alpha = 0.07f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 16.dp,
                    bottom = 16.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(
                        R.drawable.logo_ne_inicio
                    ),
                    contentDescription = "Símbolo oficial NutriExpert",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(62.dp)
                        .height(54.dp)
                )

                Spacer(Modifier.width(9.dp))

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(
                            Color.White.copy(alpha = 0.75f)
                        )
                )

                Spacer(Modifier.width(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Nutri",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Expert",
                        color = VerdeLimaInicio,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White.copy(alpha = 0.14f)
                        )
                        .clickable(onClick = onPerfil),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Abrir perfil",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¡Hola, ",
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Usuario!",
                    color = VerdeLimaInicio,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Bienvenido a tu gestión agrícola",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
        }
    }
}

private fun descripcionClima(codigo: Int): String {
    return when (codigo) {
        0 -> "Despejado"
        1 -> "Mayormente despejado"
        2 -> "Parcialmente nublado"
        3 -> "Nublado"
        45, 48 -> "Niebla"
        51, 53, 55, 56, 57 -> "Llovizna"
        61, 63, 65, 66, 67 -> "Lluvia"
        71, 73, 75, 77 -> "Nieve"
        80, 81, 82 -> "Chubascos"
        85, 86 -> "Chubascos de nieve"
        95, 96, 99 -> "Tormenta"
        else -> "Condiciones variables"
    }
}

private fun simboloClima(codigo: Int): String {
    return when (codigo) {
        0, 1 -> "☀️"
        2 -> "🌤️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55, 56, 57 -> "🌦️"
        61, 63, 65, 66, 67, 80, 81, 82 -> "🌧️"
        71, 73, 75, 77, 85, 86 -> "❄️"
        95, 96, 99 -> "⛈️"
        else -> "🌤️"
    }
}

@Composable
private fun ClimaInicio() {
    val context = LocalContext.current
    val repositorio = remember { ClimaRepository() }

    var clima by remember {
        mutableStateOf<DatosClima?>(null)
    }
    var cargando by remember {
        mutableStateOf(false)
    }
    var mensaje by remember {
        mutableStateOf("Preparando ubicación...")
    }
    var ciudad by remember {
        mutableStateOf("Ubicación actual")
    }
    var intento by remember {
        mutableIntStateOf(0)
    }
    var permisoSolicitado by remember {
        mutableStateOf(false)
    }

    val solicitarPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        permisoSolicitado = true

        if (permisos.values.any { it }) {
            intento++
        } else {
            mensaje = "Permite la ubicación para ver el clima"
        }
    }

    LaunchedEffect(intento) {
        if (!UbicacionHelper.tienePermiso(context)) {
            if (!permisoSolicitado) {
                solicitarPermisos.launch(UbicacionHelper.PERMISOS)
            } else {
                mensaje = "Permiso de ubicación no concedido"
            }
            return@LaunchedEffect
        }

        if (!UbicacionHelper.ubicacionActivada(context)) {
            mensaje = "Activa la ubicación de tu celular"
            return@LaunchedEffect
        }

        cargando = true
        mensaje = "Consultando clima..."
        clima = null

        try {
            val ubicacion = UbicacionHelper.obtenerUbicacion(context)

            if (ubicacion == null) {
                mensaje = "No se pudo obtener tu ubicación"
            } else {
                val resultado = repositorio.obtenerClima(
                    latitud = ubicacion.latitude,
                    longitud = ubicacion.longitude
                )

                if (resultado == null) {
                    mensaje = "Clima no disponible. Revisa Internet"
                } else {
                    clima = resultado
                    mensaje = ""

                    ciudad = withContext(Dispatchers.IO) {
                        try {
                            @Suppress("DEPRECATION")
                            val lugares = Geocoder(
                                context,
                                Locale("es", "CL")
                            ).getFromLocation(
                                ubicacion.latitude,
                                ubicacion.longitude,
                                1
                            )

                            lugares?.firstOrNull()?.locality
                                ?: lugares?.firstOrNull()?.subAdminArea
                                ?: "Ubicación actual"
                        } catch (e: Exception) {
                            "Ubicación actual"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            mensaje = "No se pudo consultar el clima"
        } finally {
            cargando = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = VerdeInicio,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = "Clima actual",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeOscuroInicio
                )
            }

            Spacer(Modifier.height(5.dp))

            Text(
                text = ciudad,
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(10.dp))

            val datos = clima

            if (datos != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${datos.temperatura.roundToInt()}°C",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = VerdeOscuroInicio
                        )

                        Text(
                            text = descripcionClima(datos.codigoClima),
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }

                    Text(
                        text = simboloClima(datos.codigoClima),
                        fontSize = 48.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Máx. ${datos.temperaturaMaxima.roundToInt()}°C",
                        fontSize = 13.sp,
                        color = VerdeInicio,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Mín. ${datos.temperaturaMinima.roundToInt()}°C",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            } else {
                if (cargando) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = VerdeInicio
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = mensaje,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    Text(
                        text = mensaje,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(6.dp))

                    TextButton(
                        onClick = {
                            if (UbicacionHelper.tienePermiso(context)) {
                                intento++
                            } else {
                                solicitarPermisos.launch(
                                    UbicacionHelper.PERMISOS
                                )
                            }
                        }
                    ) {
                        Text(
                            text = "Reintentar",
                            color = VerdeInicio
                        )
                    }
                }
            }
        }
    }

}


@Composable
private fun ResumenPrediosInicio(
    cantidad: Int,
    hectareas: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(
            containerColor = VerdeSuaveInicio
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Mis predios",
                    fontSize = 13.sp,
                    color = VerdeOscuroInicio
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = "$cantidad",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeOscuroInicio
                )
            }

            VerticalDivider(
                modifier = Modifier.height(40.dp),
                color = VerdeInicio.copy(alpha = 0.25f)
            )

            Column(
                modifier = Modifier.weight(1.5f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Superficie total",
                    fontSize = 13.sp,
                    color = VerdeOscuroInicio
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = "${formatoCorto(hectareas)} ha",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeOscuroInicio
                )
            }
        }
    }
}

@Composable
private fun PredioInicioCard(
    predio: PredioEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val miniatura by produceState<ImageBitmap?>(
        initialValue = null,
        predio.fotoUri
    ) {
        value = predio.fotoUri?.let {
            FotoHelper.cargarVistaPrevia(
                context,
                it,
                maxLado = 200
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (miniatura != null) {
                Image(
                    bitmap = miniatura!!,
                    contentDescription = "Foto de ${predio.nombre}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            VerdeSuaveInicio,
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = VerdeInicio,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(Modifier.width(13.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = predio.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeOscuroInicio
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = predio.comuna,
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = "${formatoCorto(predio.hectareas)} ha",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VerdeInicio
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Ver predio",
                tint = VerdeInicio,
                modifier = Modifier.rotate(-90f)
            )
        }
    }
}

@Composable
private fun BarraInferiorInicio(
    onPredios: () -> Unit,
    onPerfil: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null
                )
            },
            label = {
                Text("Inicio")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VerdeInicio,
                selectedTextColor = VerdeInicio,
                indicatorColor = VerdeSuaveInicio
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = onPredios,
            icon = {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null
                )
            },
            label = {
                Text("Mis predios")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = onPerfil,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null
                )
            },
            label = {
                Text("Perfil")
            }
        )
    }
}
