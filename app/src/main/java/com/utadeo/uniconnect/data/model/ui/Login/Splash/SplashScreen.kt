package com.utadeo.uniconnect.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val crimsonText = FontFamily(Font(R.font.crimsontext_regular))
    var showFullName by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()

    LaunchedEffect(Unit) {
        delay(1200) // muestra solo "UC"
        showFullName = true
        delay(2000) // muestra "UniConnect"
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navController.navigate(AppScreens.HomeScreen.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            navController.navigate(AppScreens.LoginScreen.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.primary_color)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo UC
            Text(
                text = "U\nC",
                fontSize = 120.sp,
                fontFamily = crimsonText,
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 110.sp
            )

            // Animación de aparición de UniConnect
            AnimatedVisibility(
                visible = showFullName,
                enter = fadeIn(animationSpec = tween(1000)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Text(
                    text = "Uni\nConnect",
                    fontSize = 50.sp,
                    fontFamily = crimsonText,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
