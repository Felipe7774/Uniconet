package com.utadeo.uniconnect.data.model.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import kotlinx.coroutines.launch

private val YellowPrimary = Color(0xFFFDD835)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAnswersScreen(
    navController: NavController,
    questionId: String
) {
    val poppins = FontFamily(Font(R.font.poppins_regular))
    val scope = rememberCoroutineScope()

    var responses by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var questionText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar respuestas y pregunta
    LaunchedEffect(questionId) {
        scope.launch {
            isLoading = true

            // Obtener la pregunta
            val question = QuestionsRepository.getQuestionById(questionId)
            questionText = question?.get("text")?.toString() ?: ""

            // Obtener respuestas
            responses = QuestionsRepository.getResponses(questionId)

            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YellowPrimary
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
                        text = "Respuestas",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }
            )
        },
        containerColor = Color(0xFFFFF9C4)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.Black)
                    }
                }
                else -> {
                    // Card con la pregunta
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                                color = YellowPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = questionText,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = Color.White,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    // Contador de respuestas
                    Text(
                        text = "${responses.size} respuesta${if (responses.size != 1) "s" else ""}",
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    // Lista de respuestas
                    if (responses.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🤔", fontSize = 64.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Aún no hay respuestas",
                                    fontFamily = poppins,
                                    fontSize = 18.sp,
                                    color = Color.Black.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Sé el primero en responder",
                                    fontFamily = poppins,
                                    fontSize = 14.sp,
                                    color = Color.Black.copy(alpha = 0.5f)
                                   )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(responses) { response ->
                                ResponseCard(
                                    response = response,
                                    poppins = poppins,
                                    onUserClick = {
                                        val userId = response["userId"] as? String
                                        if (userId != null) {
                                            navController.navigate(
                                                AppScreens.UserProfileScreen.createRoute(userId)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResponseCard(
    response: Map<String, Any>,
    poppins: FontFamily,
    onUserClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Usuario (clickeable para ir al perfil)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserClick() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(YellowPrimary.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = response["userName"]?.toString() ?: "Usuario",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Text(
                        text = getTimeAgo(response["timestamp"]),
                        fontFamily = poppins,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Respuesta
            Text(
                text = response["text"]?.toString() ?: "",
                fontFamily = poppins,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}

private fun getTimeAgo(timestamp: Any?): String {
    if (timestamp == null) return "Hace un momento"

    val time = when (timestamp) {
        is Long -> timestamp
        is com.google.firebase.Timestamp -> timestamp.toDate().time
        else -> return "Hace un momento"
    }

    val now = System.currentTimeMillis()
    val diff = now - time

    return when {
        diff < 60000 -> "Hace un momento"
        diff < 3600000 -> "Hace ${diff / 60000}m"
        diff < 86400000 -> "Hace ${diff / 3600000}h"
        diff < 604800000 -> "Hace ${diff / 86400000}d"
        else -> "Hace más de una semana"
    }
}
