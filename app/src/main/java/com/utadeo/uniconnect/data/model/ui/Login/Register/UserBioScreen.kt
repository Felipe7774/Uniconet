package com.utadeo.uniconnect.data.model.ui.Login.Register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import com.utadeo.uniconnect.data.manager.RegistrationManager

@Composable
fun UserBioScreen(navController: NavController) {
    val context = LocalContext.current
    val registrationManager = remember { RegistrationManager(context) }

    val poppins = FontFamily(Font(R.font.poppins_regular))
    val poppinsBold = FontFamily(Font(R.font.poppins_regular))

    var bioText by remember { mutableStateOf(TextFieldValue("")) }
    val maxChars = 150

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE24A))
            .padding(horizontal = 30.dp, vertical = 36.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Escribe una pequeña biografía sobre ti",
                fontFamily = poppinsBold,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "(Opcional)",
                fontFamily = poppins,
                color = Color(0xFF3D3D3D),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // TextField estilizado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF6F6F6)),
                contentAlignment = Alignment.TopStart
            ) {
                OutlinedTextField(
                    value = bioText,
                    onValueChange = {
                        if (it.text.length <= maxChars) bioText = it
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = poppins,
                        color = Color.Black
                    ),
                    placeholder = {
                        Text("Cuentale algo a la gente sobre ti...", fontFamily = poppins)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.Black
                    )
                )

                // Contador de caracteres
                Text(
                    text = "${bioText.text.length}/$maxChars",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontFamily = poppins,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Continuar
            Button(
                onClick = {
                    // ✅ MODIFICADO: Guardar biografía temporalmente
                    val bioTexto = bioText.text.trim()
                    if (bioTexto.isNotEmpty()) {
                        registrationManager.saveBiografia(bioTexto)
                    }

                    // Ir a selección de intereses
                    navController.navigate(AppScreens.InterestSelectionScreen.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    "Continuar",
                    fontFamily = poppinsBold,
                    fontSize = 18.sp
                )
            }
        }
    }
}