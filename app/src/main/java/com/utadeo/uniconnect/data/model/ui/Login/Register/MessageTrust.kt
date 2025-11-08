package com.utadeo.uniconnect.data.model.ui.Login.Register

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.utadeo.uniconnect.R
import androidx.compose.ui.tooling.preview.Preview
import com.utadeo.uniconnect.data.model.navigation.AppScreens

@Composable
fun MessageTrustScreen(navController: NavHostController? = null) {
    val activity = (LocalContext.current as? Activity)
    var showExitDialog by remember { mutableStateOf(false) }

    // Fuente Poppins
    val poppins = FontFamily(
        Font(R.font.poppins_regular),
        Font(R.font.poppins_regular, FontWeight.Medium),
        Font(R.font.poppins_regular, FontWeight.SemiBold)
    )

    // Bloquear botón "atrás"
    BackHandler {
        showExitDialog = true
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD740)), // Amarillo cálido
        color = Color(0xFFFFD740)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Texto principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 30.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = """
                        Para crear una
                        comunidad
                        segura y de
                        confianza,
                        necesitamos
                        verificar que eres
                        una persona real.
                        Este proceso es
                        rápido, privado y
                        tus datos no serán
                        compartidos con
                        nadie.
                    """.trimIndent(),
                    style = TextStyle(
                        fontFamily = poppins,
                        fontSize = 36.sp,
                        color = Color.Black,
                        lineHeight = 40.sp
                    )
                )
            }


            // Botón circular inferior derecho
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 30.dp, end = 30.dp)
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .clickable {
                        // Aquí navegas a donde quieras
                        navController?.navigate(AppScreens.IdentityVerification.route)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Continuar",
                    tint = Color(0xFFFFD740),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // Diálogo tipo iPhone
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    "¿Seguro no quieres continuar con el registro y salir de la aplicación?",
                    style = TextStyle(
                        fontFamily = poppins,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    ),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { activity?.finish() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text(
                        "Salir",
                        style = TextStyle(
                            fontFamily = poppins,
                            color = Color.White
                        )
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showExitDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD740))
                ) {
                    Text(
                        "Cancelar",
                        style = TextStyle(
                            fontFamily = poppins,
                            color = Color.Black
                        )
                    )
                }
            },
            containerColor = Color.White
        )
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    name = "Message Trust Preview"
)
@Composable
fun PreviewMessageTrustScreen() {
    MessageTrustScreen()
}
