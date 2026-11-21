
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
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.R
import cl.duoc.nutriexpert.ui.components.CampoFormulario
import cl.duoc.nutriexpert.viewmodel.RecuperarContrasenaViewModel

private val RecuperarVerdeOscuro = Color(0xFF073D24)
private val RecuperarVerdeNatural = Color(0xFF17542F)
private val RecuperarVerdeClaro = Color(0xFF8BBE45)
private val RecuperarMarfil = Color(0xFFFFFBF4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarScreen(navController: NavHostController) {

    // ViewModel original
    val recuperarViewModel: RecuperarContrasenaViewModel = viewModel()
    val estado = recuperarViewModel.estado

    Scaffold(
        containerColor = RecuperarMarfil,

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recuperar contraseña",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
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
                    containerColor = RecuperarVerdeNatural
                )
            )
        }

    ) { innerPadding ->

        // CONTENEDOR PRINCIPAL
        // Permite colocar las ondas al fondo de la pantalla.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(RecuperarMarfil)
        ) {

            // ONDAS DECORATIVAS FIJAS EN LA PARTE INFERIOR
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(70.dp)
            ) {
                val w = size.width
                val h = size.height

                val ondaFondo = Path().apply {
                    moveTo(0f, h * 0.35f)

                    quadraticBezierTo(
                        w * 0.25f,
                        h * 0.05f,
                        w * 0.52f,
                        h * 0.65f
                    )

                    quadraticBezierTo(
                        w * 0.78f,
                        h,
                        w,
                        h * 0.22f
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
                        w * 0.22f,
                        h * 0.40f,
                        w * 0.48f,
                        h * 0.88f
                    )

                    quadraticBezierTo(
                        w * 0.76f,
                        h * 1.12f,
                        w,
                        h * 0.50f
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

            // CONTENIDO DEL FORMULARIO
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 85.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(18.dp))

                // LOGO OFICIAL PEQUEÑO
                Image(
                    painter = painterResource(
                        id = R.drawable.logo_nutriexpert
                    ),
                    contentDescription = "Logo oficial NutriExpert",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(112.dp)
                        .height(75.dp)
                )

                Spacer(Modifier.height(14.dp))

                // SEPARADOR DECORATIVO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 100.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = RecuperarVerdeNatural.copy(alpha = 0.5f)
                    )

                    Text(
                        text = "🌱",
                        fontSize = 17.sp,
                        modifier = Modifier.padding(horizontal = 9.dp)
                    )

                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = RecuperarVerdeNatural.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.height(23.dp))

                // TÍTULO PRINCIPAL
                Text(
                    text = "¿Olvidaste tu\ncontraseña?",
                    fontSize = 29.sp,
                    lineHeight = 35.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                RecuperarVerdeOscuro,
                                RecuperarVerdeNatural,
                                RecuperarVerdeClaro
                            )
                        )
                    )
                )

                Spacer(Modifier.height(16.dp))

                // DESCRIPCIÓN
                Text(
                    text = "Ingresa tu correo electrónico y te ayudaremos a recuperar el acceso a tu cuenta.",
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = Color(0xFF62645F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )

                Spacer(Modifier.height(30.dp))

                // FORMULARIO
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // CAMPO DE CORREO ORIGINAL
                    CampoFormulario(
                        valor = estado.correo,
                        onValorChange = recuperarViewModel::onCorreoChange,
                        etiqueta = "Correo electrónico",
                        error = estado.mensajeError.ifEmpty { null },
                        icono = Icons.Filled.Email,
                        tipoTeclado = KeyboardType.Email
                    )

                    Spacer(Modifier.height(25.dp))

                    // BOTÓN SOLICITAR RECUPERACIÓN
                    Button(
                        onClick = {
                            recuperarViewModel.solicitarRecuperacion()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RecuperarVerdeNatural
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            RecuperarVerdeClaro,
                                            RecuperarVerdeNatural,
                                            RecuperarVerdeOscuro
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
                                    text = "Solicitar recuperación",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(Modifier.width(10.dp))

                                Icon(
                                    imageVector = Icons.Outlined.Send,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // MENSAJE INFORMATIVO ORIGINAL
                    if (estado.mensajeInformativo.isNotEmpty()) {

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = estado.mensajeInformativo,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
