package com.utadeo.uniconnect.data.model.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.chat.ChatsRepository
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val YellowPrimary = Color(0xFFFDD835)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    navController: NavController,
    userId: String
) {
    val poppins = FontFamily(Font(R.font.poppins_regular))
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val currentUserId = auth.currentUser?.uid ?: ""
    val isMyProfile = userId == currentUserId

    var isLoading by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userBio by remember { mutableStateOf("") }
    var userInterests by remember { mutableStateOf<List<String>>(emptyList()) }
    var userPhoto by remember { mutableStateOf("") }
    var isStartingChat by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        scope.launch {
            isLoading = true
            try {
                val document = db.collection("users").document(userId).get().await()
                if (document.exists()) {
                    userName = document.getString("nombre") ?: "Usuario"
                    userEmail = document.getString("email") ?: ""
                    userBio = document.getString("biografia") ?: "Sin descripción"
                    userPhoto = document.getString("photoUrl") ?: ""
                    userInterests = (document.get("intereses") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Perfil",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YellowPrimary
                )
            )
        },
        containerColor = Color(0xFFFFF9C4)
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Foto de perfil
                        if (userPhoto.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(userPhoto),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(YellowPrimary.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nombre
                        Text(
                            text = userName,
                            fontFamily = poppins,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        if (userEmail.isNotEmpty()) {
                            Text(
                                text = userEmail,
                                fontFamily = poppins,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ✅ Botón "Iniciar chat"
                        if (!isMyProfile) {
                            Button(
                                onClick = {
                                    if (!isStartingChat) {
                                        isStartingChat = true
                                        scope.launch {
                                            try {
                                                val chatId = ChatsRepository.startChat(userId, userName)
                                                isStartingChat = false
                                                navController.navigate(
                                                    AppScreens.IndividualChatScreen.createRoute(
                                                        chatId = chatId,
                                                        otherUserId = userId,
                                                        otherUserName = userName
                                                    )
                                                )
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                isStartingChat = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    disabledContainerColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !isStartingChat
                            ) {
                                if (isStartingChat) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = YellowPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = null,
                                            tint = YellowPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Iniciar chat",
                                            fontFamily = poppins,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Biografía
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "Descripción",
                                    fontFamily = poppins,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = userBio,
                                    fontFamily = poppins,
                                    fontSize = 15.sp,
                                    color = Color.Black,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Intereses
                        if (userInterests.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Text(
                                        text = "Intereses",
                                        fontFamily = poppins,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black.copy(alpha = 0.6f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    androidx.compose.foundation.layout.FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        userInterests.forEach { interest ->
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = YellowPrimary.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = interest,
                                                    fontFamily = poppins,
                                                    fontSize = 13.sp,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
