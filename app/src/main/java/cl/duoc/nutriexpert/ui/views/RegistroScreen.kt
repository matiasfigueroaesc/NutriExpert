
package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import cl.duoc.nutriexpert.ui.components.AnimacionExito
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.R
import cl.duoc.nutriexpert.ui.components.CampoFormulario
import cl.duoc.nutriexpert.viewmodel.RegistroViewModel

private val RegistroVerdeOscuro = Color(0xFF073D24)
private val RegistroVerdeNatural = Color(0xFF17542F)
private val RegistroVerdeClaro = Color(0xFF8BBE45)
private val RegistroMarfil = Color(0xFFFFFBF4)

@Composable
private fun IconoCrearUsuario(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val grosor = 2.dp.toPx()

        // Cabeza del usuario
        drawCircle(
            color = Color.White,
            radius = w * 0.14f,
            center = androidx.compose.ui.geometry.Offset(
                w * 0.35f,
                h * 0.28f
            ),
            style = Stroke(width = grosor)
        )

        // Hombros del usuario
        val hombros = Path().apply {
            moveTo(w * 0.10f, h * 0.80f)
            quadraticBezierTo(
                w * 0.10f, h * 0.52f,
                w * 0.35f, h * 0.52f
            )
            quadraticBezierTo(
                w * 0.56f, h * 0.52f,
                w * 0.59f, h * 0.72f
            )
        }

        drawPath(
            path = hombros,
            color = Color.White,
            style = Stroke(
                width = grosor,
                cap = StrokeCap.Round
            )
        )

        // Símbolo +
        drawLine(
            color = Color.White,
            start = androidx.compose.ui.geometry.Offset(
                w * 0.76f, h * 0.26f
            ),
            end = androidx.compose.ui.geometry.Offset(
                w * 0.76f, h * 0.66f
            ),
            strokeWidth = grosor,
            cap = StrokeCap.Round
        )

        drawLine(
            color = Color.White,
            start = androidx.compose.ui.geometry.Offset(
                w * 0.57f, h * 0.46f
            ),
            end = androidx.compose.ui.geometry.Offset(
                w * 0.95f, h * 0.46f
            ),
            strokeWidth = grosor,
            cap = StrokeCap.Round
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(navController: NavHostController) {

    val registroViewModel: RegistroViewModel = viewModel()
    val estado = registroViewModel.estado

    var mostrarPolitica by remember {
        mutableStateOf(false)
    }

    // Cuenta creada: la sesión ya quedó iniciada, se muestra la animación y se entra a la app
    LaunchedEffect(estado.registroCorrecto) {
        if (estado.registroCorrecto) {
            delay(1500)
            navController.navigate("inicio") {
                // Saca Login y Registro de la pila: "atrás" ya no vuelve a ellos
                popUpTo("login") { inclusive = true }
            }
        }
    }

    if (estado.registroCorrecto) {
        AnimacionExito(
            mensaje = "¡Cuenta creada!",
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Scaffold(
        containerColor = RegistroMarfil,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crear cuenta",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RegistroVerdeNatural
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(RegistroMarfil)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(8.dp))

            // LOGO OFICIAL
            Image(
                painter = painterResource(
                    id = R.drawable.logo_nutriexpert
                ),
                contentDescription = "Logo oficial NutriExpert",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(112.dp)
                    .height(68.dp)
            )

            Spacer(Modifier.height(3.dp))

            // SEPARADOR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 105.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = RegistroVerdeNatural.copy(alpha = 0.45f)
                )

                Text(
                    text = "🌱",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = RegistroVerdeNatural.copy(alpha = 0.45f)
                )
            }

            Spacer(Modifier.height(7.dp))

            // TÍTULO
            Text(
                text = "Crea tu cuenta",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            RegistroVerdeOscuro,
                            RegistroVerdeNatural,
                            RegistroVerdeClaro
                        )
                    )
                )
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = "Comienza a gestionar tus predios y cultivos.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color(0xFF62645F),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(14.dp))

            // FORMULARIO
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {

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

                // POLÍTICA DE PRIVACIDAD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = estado.aceptaPrivacidad,
                        onCheckedChange =
                            registroViewModel::onAceptaPrivacidadChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = RegistroVerdeNatural,
                            uncheckedColor = RegistroVerdeNatural
                        )
                    )

                    Text(
                        text = "Acepto la política de privacidad",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RegistroVerdeOscuro,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (estado.errores.privacidad.isNotEmpty()) {
                    Text(
                        text = estado.errores.privacidad,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TextButton(
                    onClick = {
                        mostrarPolitica = true
                    },
                    modifier = Modifier
                        .align(Alignment.Start)
                        .height(38.dp),
                    contentPadding = PaddingValues(
                        horizontal = 8.dp,
                        vertical = 0.dp
                    )
                ) {
                    Text(
                        text = "Leer política de privacidad",
                        color = RegistroVerdeNatural,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }

                if (estado.mensajeError.isNotEmpty()) {
                    Text(
                        text = estado.mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(3.dp))

                // BOTÓN CON ÍCONO DE USUARIO +
                Button(
                    onClick = {
                        registroViewModel.registrar()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RegistroVerdeNatural
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        RegistroVerdeClaro,
                                        RegistroVerdeNatural,
                                        RegistroVerdeOscuro
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Crear cuenta",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(Modifier.width(10.dp))

                            IconoCrearUsuario()
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ONDAS DECORATIVAS
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
            ) {
                val w = size.width
                val h = size.height

                val ondaFondo = Path().apply {
                    moveTo(0f, h * 0.35f)

                    quadraticBezierTo(
                        w * 0.25f, h * 0.05f,
                        w * 0.52f, h * 0.65f
                    )

                    quadraticBezierTo(
                        w * 0.78f, h,
                        w, h * 0.22f
                    )

                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                drawPath(
                    path = ondaFondo,
                    color = Color(0xFFE3EDCE)
                )

                val ondaFrontal = Path().apply {
                    moveTo(0f, h * 0.68f)

                    quadraticBezierTo(
                        w * 0.22f, h * 0.40f,
                        w * 0.48f, h * 0.88f
                    )

                    quadraticBezierTo(
                        w * 0.76f, h * 1.12f,
                        w, h * 0.50f
                    )

                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                drawPath(
                    path = ondaFrontal,
                    color = Color(0xFFB9D39A)
                )
            }
        }
    }

    // POLÍTICA DE PRIVACIDAD ORIGINAL
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
}
