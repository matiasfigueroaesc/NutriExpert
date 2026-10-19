package cl.duoc.nutriexpert.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Esquema de colores del mockup: a cada "rol" de Material 3 le asignamos un color de Color.kt
private val LightColorScheme = lightColorScheme(
    primary = VerdePrimario,
    onPrimary = Blanco,
    primaryContainer = VerdeSuave,
    onPrimaryContainer = TextoOscuro,
    secondary = AzulEnlace,
    onSecondary = Blanco,
    secondaryContainer = VerdeSuave,
    onSecondaryContainer = TextoOscuro,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = FondoClaro,
    onSurface = TextoOscuro,
    outline = Borde
)

@Composable
fun NutriExpertTheme(
    // En false para que la app use siempre los colores del mockup
    // y no los del fondo de pantalla del teléfono (Android 12+)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicLightColorScheme(LocalContext.current)
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
