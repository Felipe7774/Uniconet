package com.utadeo.uniconnect.data.model.ui.Register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import com.utadeo.uniconnect.data.manager.RegistrationManager

@Composable
fun ProfileCreationScreen(navController: NavController) {
    val context = LocalContext.current
    val registrationManager = remember { RegistrationManager(context) }

    var name by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val poppins = FontFamily(Font(R.font.poppins_regular))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.primary_color))
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Título principal
            Text(
                text = "Crea tu perfil",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.black),
                fontFamily = poppins,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo
            Text(
                text = "Elige un nombre o alias\n(este será público y las demás\npersonas lo podrán ver)",
                fontSize = 20.sp,
                color = colorResource(R.color.black),
                textAlign = TextAlign.Center,
                fontFamily = poppins
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Campo de texto para el nombre o alias
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    showError = false
                },
                placeholder = {
                    Text(
                        text = "nombre",
                        color = colorResource(R.color.gray),
                        fontFamily = poppins
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.black),
                    unfocusedBorderColor = colorResource(R.color.black),
                    focusedContainerColor = colorResource(R.color.white),
                    unfocusedContainerColor = colorResource(R.color.white),
                    cursorColor = colorResource(R.color.black)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            if (showError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = colorResource(R.color.black),
                    fontSize = 14.sp,
                    fontFamily = poppins
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón "Siguiente"
            Button(
                onClick = {
                    if (name.isEmpty()) {
                        showError = true
                        errorMessage = "Por favor ingresa un nombre o alias"
                    } else {
                        // ✅ MODIFICADO: Guardar nombre temporalmente
                        registrationManager.saveNombre(name)
                        navController.navigate(AppScreens.ProfilePictureScreen.route)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.black),
                    contentColor = colorResource(R.color.white)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Siguiente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = poppins
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}