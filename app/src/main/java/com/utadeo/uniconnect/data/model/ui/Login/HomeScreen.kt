package com.utadeo.uniconnect.data.model.ui.Login

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.activity.ActivitiesRepository
import com.utadeo.uniconnect.data.model.activity.QuestionsRepository
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import com.utadeo.uniconnect.data.model.ui.Login.notifications.NotificationsRepository
import kotlinx.coroutines.launch

private val YellowPrimary = Color(0xFFFDD835)

enum class ContentFilter {
    ALL, ACTIVITIES, QUESTIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val poppins = FontFamily(Font(R.font.poppins_regular))
    val scope = rememberCoroutineScope()

    var activities by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var questions by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedFilter by remember { mutableStateOf(ContentFilter.ALL) }

    // 🔴 NUEVO: Escuchar contador de notificaciones no leídas
    val unreadCount by NotificationsRepository.getUnreadCountFlow()
        .collectAsState(initial = 0)

    // Cargar actividades y preguntas
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            activities = ActivitiesRepository.getAllActivities()
            questions = QuestionsRepository.getQuestionsWithResponseCount()
            isLoading = false
        }
    }

    // Combinar y filtrar contenido
    val filteredContent = remember(activities, questions, selectedFilter) {
        when (selectedFilter) {
            ContentFilter.ALL -> {
                val activitiesWithType = activities.map { it + ("contentType" to "activity") }
                val questionsWithType = questions.map { it + ("contentType" to "question") }
                (activitiesWithType + questionsWithType).sortedByDescending {
                    (it["timestamp"] as? Long) ?: 0L
                }
            }
            ContentFilter.ACTIVITIES -> activities.map { it + ("contentType" to "activity") }
            ContentFilter.QUESTIONS -> questions.map { it + ("contentType" to "question") }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YellowPrimary
                ),
                title = {
                    Text(
                        text = "UniConnect",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                },
                actions = {
                    // 🔔 ACTUALIZADO: Botón de notificaciones con badge
                    Box {
                        IconButton(onClick = {
                            navController.navigate(AppScreens.NotificationsScreen.route)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color.Black
                            )
                        }

                        // 🔴 Badge contador (solo se muestra si hay notificaciones)
                        if (unreadCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-8).dp, y = 8.dp)
                            ) {
                                Text(
                                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = poppins
                                )
                            }
                        }
                    }

                    // ⚙️ Botón de configuración
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
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
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
            // Filtros superiores
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YellowPrimary)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    text = "Todo",
                    isSelected = selectedFilter == ContentFilter.ALL,
                    onClick = { selectedFilter = ContentFilter.ALL },
                    poppins = poppins,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    text = "Actividades",
                    isSelected = selectedFilter == ContentFilter.ACTIVITIES,
                    onClick = { selectedFilter = ContentFilter.ACTIVITIES },
                    poppins = poppins,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    text = "Preguntas",
                    isSelected = selectedFilter == ContentFilter.QUESTIONS,
                    onClick = { selectedFilter = ContentFilter.QUESTIONS },
                    poppins = poppins,
                    modifier = Modifier.weight(1f)
                )
            }

            // Contenido
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.Black)
                    }
                }
                filteredContent.isEmpty() -> {
                    EmptyState(
                        filter = selectedFilter,
                        navController = navController,
                        poppins = poppins
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredContent) { item ->
                            when (item["contentType"]) {
                                "activity" -> {
                                    ActivityCard(
                                        activity = item,
                                        poppins = poppins,
                                        onActivityClick = {
                                            val id = item["id"] as? String ?: return@ActivityCard
                                            navController.navigate(AppScreens.ActivityDetailScreen.createRoute(id))
                                        },
                                        onParticipantsClick = {
                                            val id = item["id"] as? String ?: return@ActivityCard
                                            navController.navigate(AppScreens.ParticipantsListScreen.createRoute(id))
                                        }
                                    )
                                }
                                "question" -> {
                                    QuestionCard(
                                        question = item,
                                        poppins = poppins,
                                        navController = navController,
                                        onQuestionClick = {
                                            val questionId = item["id"] as? String ?: return@QuestionCard
                                            navController.navigate(AppScreens.QuestionParticipateScreen.createRoute(questionId))
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
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    poppins: FontFamily,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color.Black else Color.White.copy(alpha = 0.3f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = if (isSelected) YellowPrimary else Color.Black.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun EmptyState(
    filter: ContentFilter,
    navController: NavController,
    poppins: FontFamily
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📭", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (filter) {
                ContentFilter.ALL -> "No hay contenido aún"
                ContentFilter.ACTIVITIES -> "No hay actividades aún"
                ContentFilter.QUESTIONS -> "No hay preguntas aún"
            },
            fontFamily = poppins,
            fontSize = 18.sp,
            color = Color.Black.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate(AppScreens.ActivityFlowScreen.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear contenido", fontFamily = poppins)
        }
    }
}

@Composable
fun QuestionCard(
    question: Map<String, Any>,
    poppins: FontFamily,
    navController: NavController,
    onQuestionClick: () -> Unit
) {
    val responsesCount = question["responsesCount"] as? Int ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onQuestionClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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
                        text = question["userName"]?.toString() ?: "Usuario",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Text(
                        text = getTimeAgo(question["timestamp"]),
                        fontFamily = poppins,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = question["text"]?.toString() ?: "Sin pregunta",
                fontFamily = poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = Color.Black,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color.LightGray)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val questionId = question["id"] as? String
                        if (questionId != null) {
                            navController.navigate(AppScreens.UserAnswersScreen.createRoute(questionId))
                        }
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$responsesCount respuesta${if (responsesCount != 1) "s" else ""}",
                        fontFamily = poppins,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ActivityCard(
    activity: Map<String, Any>,
    poppins: FontFamily,
    onActivityClick: () -> Unit,
    onParticipantsClick: () -> Unit
) {
    val participantsCount = activity["participantsCount"] as? Int ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onActivityClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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
                        text = activity["creatorName"]?.toString() ?: "Usuario",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Text(
                        text = getTimeAgo(activity["timestamp"]),
                        fontFamily = poppins,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = activity["title"]?.toString() ?: "Sin título",
                fontFamily = poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activity["description"]?.toString() ?: "",
                fontFamily = poppins,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.7f),
                lineHeight = 20.sp,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.CalendarToday,
                    text = activity["date"]?.toString() ?: "Fecha",
                    poppins = poppins
                )
                InfoChip(
                    icon = Icons.Default.AccessTime,
                    text = activity["time"]?.toString() ?: "Hora",
                    poppins = poppins
                )
            }

            if ((activity["locationName"] as? String)?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                InfoChip(
                    icon = Icons.Default.LocationOn,
                    text = activity["locationName"]?.toString() ?: "",
                    poppins = poppins,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color.LightGray)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onParticipantsClick() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$participantsCount participante${if (participantsCount != 1) "s" else ""}",
                        fontFamily = poppins,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: ImageVector,
    text: String,
    poppins: FontFamily,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                fontFamily = poppins,
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
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