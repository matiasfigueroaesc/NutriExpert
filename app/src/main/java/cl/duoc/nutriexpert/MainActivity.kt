package cl.duoc.nutriexpert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import cl.duoc.nutriexpert.navigation.AppNavigation
import cl.duoc.nutriexpert.ui.theme.NutriExpertTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutriExpertTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}
