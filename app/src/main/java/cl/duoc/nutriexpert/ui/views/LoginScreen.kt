
package cl.duoc.nutriexpert.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.duoc.nutriexpert.R
import cl.duoc.nutriexpert.ui.components.AnimacionExito
import cl.duoc.nutriexpert.ui.components.CampoFormulario
import cl.duoc.nutriexpert.viewmodel.LoginViewModel
import kotlinx.coroutines.delay

private val VerdeNatural = Color(0xFF17542F)
private val VerdeOscuro = Color(0xFF073D24)
private val Marfil = Color(0xFFFFFBF4)
private val VerdeClaro = Color(0xFFE6EED7)

@Composable
fun LoginScreen(navController: NavHostController) {

    val loginViewModel: LoginViewModel = viewModel()
    val estado = loginViewModel.estado

    var mostrarAnimacion by remember {
        mutableStateOf(false)
    }

    var mostrarContenido by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        mostrarContenido = true
    }

    LaunchedEffect(estado.ingresoCorrecto) {
        if (estado.ingresoCorrecto) {
            mostrarAnimacion = true
            delay(1500)

            navController.navigate("predios") {
                popUpTo("login") {
                    inclusive = true
                }
            }
        }
    }

    if (mostrarAnimacion) {
        AnimacionExito(
            mensaje = "¡Bienvenido a NutriExpert!",
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Marfil)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // PAISAJE AGRÍCOLA Y LOGO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(275.dp)
        ) {

            Image(
                painter = painterResource(
                    R.drawable.paisaje_login_nutriexpert
                ),
                contentDescription = "Paisaje agrícola",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // DEGRADADO ORIGINAL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Marfil.copy(alpha = 0.90f),
                                Marfil.copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // LOGO OFICIAL Y SUBTÍTULO
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 38.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(
                        R.drawable.logo_nutriexpert
                    ),
                    contentDescription = "Logo oficial NutriExpert",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(220.dp)
                        .height(112.dp)
                )

                // BURBUJA MÁS PEQUEÑA
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .background(
                            color = Marfil.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(
                            horizontal = 12.dp,
                            vertical = 5.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Fertirriego & Programación",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VerdeOscuro,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // CURVA INFERIOR ORIGINAL
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                val w = size.width
                val h = size.height

                val curva = Path().apply {
                    moveTo(0f, h * 0.68f)
                    quadraticBezierTo(
                        w * 0.25f, 0f,
                        w * 0.53f, h * 0.45f
                    )
                    quadraticBezierTo(
                        w * 0.80f, h * 0.95f,
                        w, h * 0.12f
                    )
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                drawPath(
                    path = curva,
                    color = Marfil
                )
            }
        }

        // FORMULARIO ANIMADO ORIGINAL
        AnimatedVisibility(
            visible = mostrarContenido,
            enter = fadeIn() +
                    slideInVertically(
                        initialOffsetY = { it / 12 }
                    )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "¡Bienvenido!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = VerdeOscuro
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Gestiona tus predios, sectores y análisis de forma sencilla.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF555B55),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                // CORREO ELECTRÓNICO
                CampoFormulario(
                    valor = estado.correo,
                    onValorChange = loginViewModel::onCorreoChange,
                    etiqueta = "Correo electrónico",
                    error = estado.errores.correo,
                    icono = Icons.Filled.Email,
                    tipoTeclado = KeyboardType.Email
                )

                Spacer(Modifier.height(12.dp))

                // CONTRASEÑA
                CampoFormulario(
                    valor = estado.contrasena,
                    onValorChange = loginViewModel::onContrasenaChange,
                    etiqueta = "Contraseña",
                    error = estado.errores.contrasena,
                    icono = Icons.Filled.Lock,
                    esContrasena = true
                )

                if (estado.mensajeError.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = estado.mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // RECUPERAR CONTRASEÑA
                TextButton(
                    onClick = {
                        navController.navigate("recuperar")
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        fontSize = 13.sp,
                        color = VerdeOscuro
                    )
                }

                Spacer(Modifier.height(14.dp))

                // BOTÓN INGRESAR
                Button(
                    onClick = {
                        loginViewModel.ingresar()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdeNatural
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF276A35),
                                        VerdeNatural
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ingresar",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // CREAR CUENTA
                OutlinedButton(
                    onClick = {
                        navController.navigate("registro")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        1.3.dp,
                        VerdeNatural
                    )
                ) {
                    Text(
                        text = "Crear cuenta",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VerdeOscuro
                    )
                }

                Spacer(Modifier.height(14.dp))

                // TUTORIAL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = VerdeNatural.copy(alpha = 0.35f)
                    )

                    TextButton(
                        onClick = {
                            // Tutorial pendiente de implementar
                        }
                    ) {
                        Text(
                            text = "Ver tutorial",
                            color = VerdeOscuro,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = VerdeNatural.copy(alpha = 0.35f)
                    )
                }

                Spacer(Modifier.height(18.dp))

                // MENSAJE INFERIOR SIN ICONO WIFI
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            VerdeClaro,
                            RoundedCornerShape(22.dp)
                        )
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Después del primer ingreso,\nla app funciona sin conexión.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = VerdeOscuro,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
