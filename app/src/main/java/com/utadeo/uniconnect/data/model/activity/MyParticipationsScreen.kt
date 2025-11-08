package com.utadeo.uniconnect.data.model.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.activity.ActivitiesRepository
import com.utadeo.uniconnect.data.model.activity.QuestionsRepository
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

private val YellowPrimary = Color(0xFFFDD835)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyParticipationsScreen(navController: NavController) {
    val poppins = FontFamily(Font(R.font.poppins_regular))
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    var selectedTab by remember { mutableStateOf(0) }
    var myActivities by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var myQuestions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedNavTab by remember { mutableStateOf(2) }

    LaunchedEffect(selectedTab) {
        scope.launch {
            isLoading = true

            if (selectedTab == 0) {
                // Cargar mis actividades
                myActivities = ActivitiesRepository.getMyActivities()
            } else {
                // Cargar mis preguntas respondidas
                val currentUserId = auth.currentUser?.uid ?: ""
                val currentUserName = auth.currentUser?.displayName
                    ?: auth.currentUser?.email
                    ?: "Tú"

                val allQuestions = QuestionsRepository.getQuestions()
                myQuestions = allQuestions.filter { question ->
                    val questionId = question["id"] as? String ?: return@filter false
                    val responses = runCatching {
                        QuestionsRepository.getResponses(questionId)
                    }.getOrDefault(emptyList())

                    responses.any { response ->
                        response["userName"] == currentUserName
                    }
                }
            }

            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis participaciones",
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
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedNavTab == 0,
                    onClick = {
                        selectedNavTab = 0
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
                    selected = selectedNavTab == 1,
                    onClick = {
                        selectedNavTab = 1
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
                    selected = selectedNavTab == 2,
                    onClick = {
                        selectedNavTab = 2
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
                    selected = selectedNavTab == 3,
                    onClick = {
                        selectedNavTab = 3
                        navController.navigate(AppScreens.UserProfileScreen.myProfile())
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
        },
        floatingActionButton = {
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
        },
        containerColor = Color(0xFFFFF9C4)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color.Black,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = YellowPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Actividades",
                            fontFamily = poppins,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Preguntas",
                            fontFamily = poppins,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Contenido
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Black
                        )
                    }
                    selectedTab == 0 && myActivities.isEmpty() -> {
                        EmptyState(
                            icon = Icons.Default.EventBusy,
                            message = "No estás en ninguna actividad aún",
                            poppins = poppins
                        )
                    }
                    selectedTab == 1 && myQuestions.isEmpty() -> {
                        EmptyState(
                            icon = Icons.Default.QuestionAnswer,
                            message = "No has respondido ninguna pregunta",
                            poppins = poppins
                        )
                    }
                    selectedTab == 0 -> {
                        // Lista de actividades
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(myActivities) { activity ->
                                MyActivityCard(
                                    activity = activity,
                                    poppins = poppins,
                                    onClick = {
                                        val id = activity["id"] as? String ?: return@MyActivityCard
                                        navController.navigate(
                                            AppScreens.ActivityDetailScreen.createRoute(id)
                                        )
                                    }
                                )
                            }
                        }
                    }
                    else -> {
                        // Lista de preguntas
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(myQuestions) { question ->
                                MyQuestionCard(
                                    question = question,
                                    poppins = poppins
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
fun EmptyState(
    icon: ImageVector,
    message: String,
    poppins: FontFamily
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Black.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontFamily = poppins,
            fontSize = 16.sp,
            color = Color.Black.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun MyActivityCard(
    activity: Map<String, Any>,
    poppins: FontFamily,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = activity["title"]?.toString() ?: "Sin título",
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = YellowPrimary.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${activity["participantsCount"] ?: 0}",
                            fontFamily = poppins,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activity["description"]?.toString() ?: "",
                fontFamily = poppins,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.7f),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChipSmall(
                    icon = Icons.Default.CalendarToday,
                    text = activity["date"]?.toString() ?: "",
                    poppins = poppins
                )
                InfoChipSmall(
                    icon = Icons.Default.AccessTime,
                    text = activity["time"]?.toString() ?: "",
                    poppins = poppins
                )
            }
        }
    }
}

@Composable
fun MyQuestionCard(
    question: Map<String, Any>,
    poppins: FontFamily
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(YellowPrimary.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = question["userName"]?.toString() ?: "Anónimo",
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = question["text"]?.toString() ?: "",
                fontFamily = poppins,
                fontSize = 15.sp,
                color = Color.Black,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Has respondido esta pregunta",
                    fontFamily = poppins,
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun InfoChipSmall(
    icon: ImageVector,
    text: String,
    poppins: FontFamily
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                fontFamily = poppins,
                fontSize = 11.sp,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
    }
}