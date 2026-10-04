package cl.duoc.nutriexpert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import cl.duoc.nutriexpert.ui.theme.NutriExpertTheme
import cl.duoc.nutriexpert.ui.views.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutriExpertTheme {
                // Temporal: en la parte 1c esto se reemplaza por AppNavigation
                LoginScreen(navController = rememberNavController())
            }
        }
    }
}
