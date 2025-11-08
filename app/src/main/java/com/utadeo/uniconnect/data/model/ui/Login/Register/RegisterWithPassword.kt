package com.utadeo.uniconnect.data.model.ui.Register

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import com.utadeo.uniconnect.ui.register.RegisterState
import com.utadeo.uniconnect.ui.register.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterWithPasswordScreen(
    navController: NavController,
    email: String
) {
    val viewModel: RegisterViewModel = hiltViewModel()
    val registerState by viewModel.registerState.collectAsState()

    var password by remember { mutableStateOf("") }

    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> {
                navController.navigate(AppScreens.MessageTrust.route) {
                    popUpTo(AppScreens.LoginScreen.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            is RegisterState.Error -> {
                val errorMessage = (registerState as RegisterState.Error).message
                Log.e("RegisterDebug", "Error de registro: $errorMessage")
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colorResource(R.color.primary_color),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver atrás",
                            modifier = Modifier.size(24.dp),
                            tint = colorResource(R.color.black)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.primary_color),
                    navigationIconContentColor = colorResource(R.color.black)
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 30.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Crea una contraseña",
                fontSize = 32.sp,
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                color = colorResource(R.color.black),
                textAlign = TextAlign.Center,
                lineHeight = 38.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Contraseña",
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                color = colorResource(R.color.black),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(text = "Ingresa tu contraseña", color = colorResource(R.color.gray))
                },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(55.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorResource(R.color.white),
                    unfocusedContainerColor = colorResource(R.color.white),
                    disabledContainerColor = colorResource(R.color.white),
                    focusedIndicatorColor = colorResource(R.color.gray),
                    unfocusedIndicatorColor = colorResource(R.color.gray),
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "La contraseña debe contener al menos:",
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    color = colorResource(R.color.black),
                    textAlign = TextAlign.Start
                )
                Text("• 10 caracteres", fontSize = 14.sp)
                Text("• 1 letra mayúscula y minúscula", fontSize = 14.sp)
                Text("• 1 número o carácter especial", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(60.dp))

            Button(
                onClick = {
                    if (isValidPassword(password)) {
                        Log.d("RegisterDebug", "Intentando registrar con email=$email y password=$password")
                        viewModel.registerWithEmailAndPassword(email, password)
                    } else {
                        Log.w("RegisterDebug", "Contraseña inválida")
                    }
                },
                enabled = registerState !is RegisterState.Loading && isValidPassword(password),
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.black),
                    contentColor = colorResource(R.color.white)
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                if (registerState is RegisterState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colorResource(R.color.white)
                    )
                } else {
                    Text(
                        text = "Siguiente",
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.poppins_regular))
                    )
                }
            }
        }
    }
}

private fun isValidPassword(password: String): Boolean {
    if (password.length < 10) return false
    if (!password.any { it.isUpperCase() }) return false
    if (!password.any { it.isLowerCase() }) return false
    if (!password.any { it.isDigit() || !it.isLetterOrDigit() }) return false
    return true
}
