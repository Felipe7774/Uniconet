package com.utadeo.uniconnect.data.model.ui.Login.Register

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import com.utadeo.uniconnect.data.manager.RegistrationManager

@Composable
fun InterestSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val registrationManager = remember { RegistrationManager(context) }

    val intereses = listOf(
        "Cine y series" to R.drawable.ic_movie,
        "Arte" to R.drawable.ic_art,
        "Animales" to R.drawable.ic_cat,
        "Viajar" to R.drawable.ic_travel,
        "Comida" to R.drawable.ic_food,
        "Videojuegos" to R.drawable.ic_game,
        "Música" to R.drawable.ic_music,
        "Lectura" to R.drawable.ic_book,
        "Deporte" to R.drawable.ic_sport,
        "Naturaleza" to R.drawable.ic_nature
    )

    val selected = remember { mutableStateListOf<String>() }
    var processing by remember { mutableStateOf(false) }

    BackHandler {
        navController.popBackStack(
            route = AppScreens.SplashScreen.route,
            inclusive = false
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD740))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Elige tus intereses",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(Modifier.height(24.dp))

        intereses.chunked(2).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                fila.forEach { (nombre, icono) ->
                    val isSelected = nombre in selected
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(100))
                            .background(if (isSelected) Color.Black else Color.White)
                            .clickable {
                                if (isSelected) selected.remove(nombre)
                                else selected.add(nombre)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(icono),
                                contentDescription = nombre,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = nombre,
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (selected.size >= 3) {
                    processing = true

                    // ✅ MODIFICADO: Solo guardar intereses temporalmente, NO en Firestore
                    registrationManager.saveIntereses(selected)

                    // Navegar a pantalla de confirmación
                    navController.navigate(AppScreens.RegistroCompletadoScreen.route) {
                        launchSingleTop = true
                    }
                }
            },
            enabled = selected.size >= 3 && !processing,
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                disabledContainerColor = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text(
                text = if (processing) "Guardando..." else "Continuar",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Muestra un mensaje si hay menos de 3 intereses seleccionados
        if (selected.size < 3) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Selecciona al menos 3 intereses",
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(30.dp))
    }
}