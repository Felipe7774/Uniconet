package com.utadeo.uniconnect.data.model.ui.Login.Register

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import com.utadeo.uniconnect.data.manager.RegistrationManager
import kotlinx.coroutines.launch

/**
 * ⚠️ PANTALLA CRÍTICA - AQUÍ SE EJECUTA TODO EL REGISTRO
 * 1. Crea cuenta Firebase Auth
 * 2. Sube fotos a Cloudinary
 * 3. Guarda todo en Firestore
 * 4. Marca registrationCompleted = true
 */
@Composable
fun RegistroCompletadoScreen(navController: NavController) {
    val context = LocalContext.current
    val registrationManager = remember { RegistrationManager(context) }
    val scope = rememberCoroutineScope()

    var progressMessage by remember { mutableStateOf("Preparando tu registro...") }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var registrationComplete by remember { mutableStateOf(false) }

    // Ejecutar el registro completo al entrar a esta pantalla
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                println("🟢 RegistroCompletadoScreen: Iniciando registro completo...")

                val result = registrationManager.completeRegistration(
                    onProgress = { message ->
                        progressMessage = message
                        println("🔄 Progreso: $message")
                    }
                )

                result.onSuccess { userId ->
                    println("✅ Registro completado exitosamente: $userId")
                    progressMessage = "¡Registro completado exitosamente!"
                    registrationComplete = true

                    // Esperar 2 segundos y navegar
                    kotlinx.coroutines.delay(2000)

                    navController.navigate(AppScreens.ActivityFlowScreen.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }

                result.onFailure { error ->
                    println("❌ Error en registro: ${error.message}")
                    hasError = true
                    errorMessage = error.message ?: "Error desconocido"
                    progressMessage = "Error al completar registro"

                    Toast.makeText(
                        context,
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                println("❌ Excepción en registro: ${e.message}")
                hasError = true
                errorMessage = e.message ?: "Error inesperado"
                progressMessage = "Error inesperado"

                Toast.makeText(
                    context,
                    "Error inesperado: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD740)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {

            if (!hasError) {
                // Modo de carga normal
                CircularProgressIndicator(
                    color = Color.Black,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (registrationComplete) "¡Listo!" else "Completando registro...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = progressMessage,
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Por favor espera, esto puede tomar unos momentos...",
                    fontSize = 14.sp,
                    color = Color.Black.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )

            } else {
                // Modo de error
                Text(
                    text = "⚠️",
                    fontSize = 64.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Error al completar registro",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = errorMessage,
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                androidx.compose.material3.Button(
                    onClick = {
                        // Reintentar
                        hasError = false
                        progressMessage = "Reintentando..."

                        scope.launch {
                            try {
                                val result = registrationManager.completeRegistration(
                                    onProgress = { message -> progressMessage = message }
                                )

                                result.onSuccess {
                                    registrationComplete = true
                                    kotlinx.coroutines.delay(2000)
                                    navController.navigate(AppScreens.ActivityFlowScreen.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }

                                result.onFailure { error ->
                                    hasError = true
                                    errorMessage = error.message ?: "Error desconocido"
                                }
                            } catch (e: Exception) {
                                hasError = true
                                errorMessage = e.message ?: "Error inesperado"
                            }
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    Text("Reintentar", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                androidx.compose.material3.TextButton(
                    onClick = {
                        // Volver al inicio
                        scope.launch {
                            registrationManager.cancelRegistration()
                            navController.navigate(AppScreens.LoginScreen.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text("Volver al inicio", color = Color.Black)
                }
            }
        }
    }
}