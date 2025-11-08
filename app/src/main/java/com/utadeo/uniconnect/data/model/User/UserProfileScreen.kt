package com.utadeo.uniconnect.data.model.User

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
fun UserProfileScreen(
    navController: NavController,
    userId: String = "me"
) {
    val poppins = FontFamily(Font(R.font.poppins_regular))
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val currentUserId = auth.currentUser?.uid ?: ""
    val isMyProfile = userId == "me" || userId == currentUserId

    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userBio by remember { mutableStateOf("") }
    var userInterests by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isCreatingChat by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(3) }

    LaunchedEffect(userId, isMyProfile) {
        scope.launch {
            isLoading = true

            try {
                if (isMyProfile) {
                    val currentUser = auth.currentUser
                    userEmail = currentUser?.email ?: ""

                    val doc = firestore.collection("users")
                        .document(currentUserId)
                        .get()
                        .await()

                    userName = doc.getString("displayName")
                        ?: doc.getString("nombre")
                                ?: currentUser?.displayName
                                ?: "Usuario"

                    userBio = doc.getString("bio")
                        ?: doc.getString("biografia")
                                ?: "Sin biografía"

                    userInterests = (doc.get("interests") as? List<*>)?.mapNotNull { it as? String }
                        ?: (doc.get("intereses") as? List<*>)?.mapNotNull { it as? String }
                                ?: emptyList()
                } else {
                    val doc = firestore.collection("users")
                        .document(userId)
                        .get()
                        .await()

                    userName = doc.getString("displayName")
                        ?: doc.getString("nombre")
                                ?: doc.getString("name")
                                ?: "Usuario"

                    userEmail = doc.getString("email") ?: ""

                    userBio = doc.getString("bio")
                        ?: doc.getString("biografia")
                                ?: "Sin biografía"

                    userInterests = (doc.get("interests") as? List<*>)?.mapNotNull { it as? String }
                        ?: (doc.get("intereses") as? List<*>)?.mapNotNull { it as? String }
                                ?: emptyList()
                }
            } catch (e: Exception) {
                userName = "Error al cargar"
                userEmail = ""
                userBio = "No se pudo cargar la información"
            }

            isLoading = false
        }
    }

    // ✅ Función para iniciar chat
    fun startChatWithUser() {
        if (isCreatingChat) return

        isCreatingChat = true
        scope.launch {
            try {
                val chatId = ChatsRepository.startChat(userId, userName)
                navController.navigate("individual_chat/$chatId/$userId/$userName")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isCreatingChat = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YellowPrimary
                ),
                navigationIcon = {
                    if (!isMyProfile) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.Black
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = if (isMyProfile) "Mi Perfil" else "Perfil",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                actions = {
                    if (isMyProfile) {
                        IconButton(onClick = {
                            navController.navigate(AppScreens.SettingsScreen.route)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configuración",
                                tint = Color.Black
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isMyProfile) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            navController.navigate(AppScreens.HomeScreen.route)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Inicio",
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                "Inicio",
                                fontFamily = poppins,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = YellowPrimary,
                            selectedTextColor = Color.Black,
                            indicatorColor = YellowPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            navController.navigate(AppScreens.ChatsListScreen.route)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Chats",
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                "Chats",
                                fontFamily = poppins,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = YellowPrimary,
                            selectedTextColor = Color.Black,
                            indicatorColor = YellowPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            navController.navigate(AppScreens.MyParticipationsScreen.route)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Mis actividades",
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                "Mis\nactividades",
                                fontFamily = poppins,
                                fontSize = 10.sp,
                                maxLines = 2,
                                lineHeight = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = YellowPrimary,
                            selectedTextColor = Color.Black,
                            indicatorColor = YellowPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = {
                            selectedTab = 3
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Perfil",
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                "Perfil",
                                fontFamily = poppins,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = YellowPrimary,
                            selectedTextColor = Color.Black,
                            indicatorColor = YellowPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (isMyProfile) {
                FloatingActionButton(
                    onClick = { navController.navigate(AppScreens.ActivityFlowScreen.route) },
                    containerColor = Color.Black,
                    contentColor = YellowPrimary,
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(8.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFFFFF9C4)
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
            else -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = userName,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (userEmail.isNotEmpty()) {
                        Text(
                            text = userEmail,
                            fontFamily = poppins,
                            fontSize = 14.sp,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // ✅ BOTÓN DE CHAT (solo si NO es mi perfil)
                    if (!isMyProfile) {
                        Button(
                            onClick = { startChatWithUser() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isCreatingChat
                        ) {
                            if (isCreatingChat) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = YellowPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = YellowPrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Enviar mensaje",
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = YellowPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Biografía",
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = userBio,
                                fontFamily = poppins,
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.7f),
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (userInterests.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Intereses",
                                        fontFamily = poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    userInterests.forEach { interest ->
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = YellowPrimary.copy(alpha = 0.3f)
                                        ) {
                                            Text(
                                                text = interest,
                                                fontFamily = poppins,
                                                fontSize = 13.sp,
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isMyProfile) {
                        Button(
                            onClick = {
                                auth.signOut()
                                navController.navigate(AppScreens.LoginScreen.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Cerrar sesión",
                                fontFamily = poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
        content()
    }
}