package com.utadeo.uniconnect.data.model.ui.Login

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import com.utadeo.uniconnect.data.manager.RegistrationManager

@Composable
fun IdentityVerification(navController: NavController) {
    val context = LocalContext.current
    val registrationManager = remember { RegistrationManager(context) }

    val poppins = FontFamily(Font(R.font.poppins_regular))
    val poppinsBold = FontFamily(Font(R.font.poppins_regular))

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isVerified by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Coloca tu cara frente a la cámara") }
    var processing by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE24A))
            .padding(horizontal = 28.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Verifica tu identidad",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = poppinsBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Asegúrate de que tu rostro esté bien iluminado y centrado dentro del marco.",
                    fontSize = 18.sp,
                    color = Color(0xFF3D3D3D),
                    textAlign = TextAlign.Center,
                    fontFamily = poppins,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Cámara centrada
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFBE98C)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        CameraXPreview(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape),
                            onVerified = { bitmap ->
                                isVerified = true
                                statusText = "Guardando imagen..."
                                processing = true

                                // ✅ MODIFICADO: Guardar localmente, NO subir a Cloudinary
                                try {
                                    registrationManager.saveVerificationImage(bitmap)
                                    statusText = "✓ Identidad capturada correctamente"
                                    processing = false
                                } catch (e: Exception) {
                                    statusText = "Error al guardar imagen: ${e.message}"
                                    processing = false
                                    isVerified = false
                                }
                            },
                            onStatus = { st -> statusText = st }
                        )
                    } else {
                        Text(
                            "Permiso de cámara requerido",
                            fontSize = 16.sp,
                            fontFamily = poppins,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    statusText,
                    fontSize = 18.sp,
                    fontFamily = poppins,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        navController.navigate(AppScreens.ProfileCreationScreen.route)
                    },
                    enabled = isVerified && !processing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVerified) Color.Black else Color(0xFF444444),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        if (processing) "Procesando..." else "Continuar",
                        fontFamily = poppinsBold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Tu foto se guardará de forma segura y solo se subirá al completar el registro.",
                    fontSize = 14.sp,
                    fontFamily = poppins,
                    color = Color(0xFF3C3C3C),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}