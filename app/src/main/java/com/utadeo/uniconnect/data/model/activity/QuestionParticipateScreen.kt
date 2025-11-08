package com.utadeo.uniconnect.data.model.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.activity.QuestionsRepository
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionParticipateScreen(
    navController: NavController,
    questionId: String
) {
    val poppins = FontFamily(Font(R.font.poppins_regular))
    val scope = rememberCoroutineScope()

    var questionText by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var hasResponded by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val maxCharacters = 150
    val minCharacters = 5

    // Cargar la pregunta y verificar si ya respondió
    LaunchedEffect(questionId) {
        scope.launch {
            isLoading = true

            val question = QuestionsRepository.getQuestionById(questionId)
            questionText = question?.get("text")?.toString() ?: ""

            hasResponded = QuestionsRepository.hasUserResponded(questionId)

            isLoading = false
        }
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "¡Respuesta enviada!",
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Tu respuesta ha sido publicada exitosamente.",
                    fontFamily = poppins
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        // 🔥 Navegar a Home limpiando el backstack
                        navController.navigate(AppScreens.HomeScreen.route) {
                            popUpTo(AppScreens.HomeScreen.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text("Aceptar", fontFamily = poppins)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFDD835)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                title = {
                    Text(
                        text = "Responder",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }
            )
        },
        containerColor = Color(0xFFFDD835)
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Black)
                }
            }
            hasResponded -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("✅", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ya respondiste esta pregunta",
                            fontFamily = poppins,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            )
                        ) {
                            Text("Volver", fontFamily = poppins)
                        }
                    }

                    // 🔥 Código agregado tal cual lo pediste:
                    if (hasResponded) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(28.dp)
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .clickable {
                                    // Ir a Home y limpiar todo el backstack
                                    navController.navigate(AppScreens.HomeScreen.route) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Continuar",
                                tint = Color(0xFFFDD835),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // Card con la pregunta
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Pregunta",
                                fontFamily = poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFFDD835)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = questionText,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = Color.White,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Tu respuesta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = poppins,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            TextField(
                                value = responseText,
                                onValueChange = {
                                    if (it.length <= maxCharacters) {
                                        responseText = it
                                        errorMessage = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = poppins,
                                    color = Color.Black
                                ),
                                placeholder = {
                                    Text(
                                        text = "Escribe tu respuesta aquí...",
                                        fontFamily = poppins,
                                        color = Color.Gray
                                    )
                                },
                                enabled = !isSubmitting
                            )

                            Text(
                                text = "${responseText.length}/$maxCharacters",
                                fontSize = 12.sp,
                                fontFamily = poppins,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ $errorMessage",
                            fontSize = 14.sp,
                            fontFamily = poppins,
                            color = Color.Red,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            when {
                                responseText.length < minCharacters -> {
                                    errorMessage = "La respuesta debe tener al menos $minCharacters caracteres"
                                }
                                responseText.isBlank() -> {
                                    errorMessage = "La respuesta no puede estar vacía"
                                }
                                else -> {
                                    isSubmitting = true
                                    scope.launch {
                                        try {
                                            QuestionsRepository.addResponse(questionId, responseText)
                                            showSuccessDialog = true
                                        } catch (e: Exception) {
                                            errorMessage = "Error al enviar la respuesta"
                                            isSubmitting = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            disabledContainerColor = Color.Black.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isSubmitting && responseText.length >= minCharacters
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviando...", fontFamily = poppins, fontSize = 16.sp)
                        } else {
                            Text(
                                text = "Publicar respuesta",
                                fontFamily = poppins,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
