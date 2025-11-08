package com.utadeo.uniconnect.data.model.ui.Login.notifications

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import com.utadeo.uniconnect.data.model.ui.Login.notifications.AppNotification
import com.utadeo.uniconnect.data.model.ui.Login.notifications.NotificationType
import com.utadeo.uniconnect.data.model.ui.Login.notifications.NotificationsRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
private val YellowPrimary = Color(0xFFFDD835)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val poppins = FontFamily(Font(R.font.poppins_regular))
    val scope = rememberCoroutineScope()

    // 🔴 Escuchar notificaciones en tiempo real
    val notifications by NotificationsRepository.getUserNotificationsFlow()
        .collectAsState(initial = emptyList())

    var showDeleteDialog by remember { mutableStateOf(false) }
    var notificationToDelete by remember { mutableStateOf<String?>(null) }

    // Dialog de confirmación para eliminar
    if (showDeleteDialog && notificationToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "¿Eliminar notificación?",
                    fontFamily = poppins,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Esta acción no se puede deshacer",
                    fontFamily = poppins
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            NotificationsRepository.deleteNotification(notificationToDelete!!)
                            showDeleteDialog = false
                            notificationToDelete = null
                        }
                    }
                ) {
                    Text("Eliminar", fontFamily = poppins, color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", fontFamily = poppins)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YellowPrimary
                ),
                title = {
                    Text(
                        text = "Notificaciones",
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    // Marcar todas como leídas
                    if (notifications.any { !it.isRead }) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    NotificationsRepository.markAllAsRead()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = "Marcar todas como leídas",
                                tint = Color.Black
                            )
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFFFFF9C4)
    ) { padding ->
        if (notifications.isEmpty()) {
            // Estado vacío
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Black.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No hay notificaciones",
                        fontFamily = poppins,
                        fontSize = 18.sp,
                        color = Color.Black.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Aquí aparecerán tus mensajes,\nnuevas participaciones y respuestas",
                        fontFamily = poppins,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        poppins = poppins,
                        onClick = {
                            // Marcar como leída
                            scope.launch {
                                NotificationsRepository.markAsRead(notification.id)
                            }

                            // Navegar según el tipo
                            when (notification.type) {
                                NotificationType.NEW_MESSAGE -> {
                                    // Navegar al chat
                                    navController.navigate(
                                        AppScreens.IndividualChatScreen.createRoute(
                                            chatId = notification.targetId,
                                            otherUserId = notification.fromUserId,
                                            otherUserName = notification.fromUserName
                                        )
                                    )
                                }
                                NotificationType.NEW_PARTICIPANT -> {
                                    // Navegar a detalles de actividad
                                    navController.navigate(
                                        AppScreens.ActivityDetailScreen.createRoute(notification.targetId)
                                    )
                                }
                                NotificationType.NEW_ANSWER -> {
                                    // Navegar a respuestas de pregunta
                                    navController.navigate(
                                        AppScreens.UserAnswersScreen.createRoute(notification.targetId)
                                    )
                                }
                            }
                        },
                        onDelete = {
                            notificationToDelete = notification.id
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: AppNotification,
    poppins: FontFamily,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, iconColor) = when (notification.type) {
        NotificationType.NEW_MESSAGE -> Icons.Default.Message to Color(0xFF4CAF50)
        NotificationType.NEW_PARTICIPANT -> Icons.Default.People to Color(0xFF2196F3)
        NotificationType.NEW_ANSWER -> Icons.Default.QuestionAnswer to Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFFFF59D)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono del tipo de notificación
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenido
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.fromUserName,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (!notification.isRead) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    color = Color.Black.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                if (notification.targetTitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${notification.targetTitle}\"",
                        fontFamily = poppins,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formatNotificationTime(notification.timestamp.toDate()),
                    fontFamily = poppins,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Botón eliminar
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatNotificationTime(date: Date): String {
    val now = Date()
    val diff = now.time - date.time

    return when {
        diff < 60000 -> "Ahora"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        diff < 604800000 -> "${diff / 86400000}d"
        else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
    }
}