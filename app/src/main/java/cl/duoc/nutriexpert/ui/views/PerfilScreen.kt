
package cl.duoc.nutriexpert.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

private val PerfilVerde = Color(0xFF17542F)
private val PerfilMarfil = Color(0xFFFFFBF4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavHostController) {

    Scaffold(
        containerColor = PerfilMarfil,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi perfil",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
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
                    containerColor = PerfilVerde
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Color(0xFFE6EED7),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Usuario",
                    tint = PerfilVerde,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "Mi perfil",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = PerfilVerde
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Próximamente",
                fontSize = 15.sp,
                color = Color.Gray
            )
        }
    }
}
