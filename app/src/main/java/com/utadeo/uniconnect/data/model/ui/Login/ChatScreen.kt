package com.utadeo.uniconnect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.utadeo.uniconnect.data.model.navigation.AppScreens
import kotlinx.coroutines.launch

data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderEmail: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, chatId: String?) {
    val db = Firebase.firestore
    val currentUser = Firebase.auth.currentUser
    val userChatId = chatId ?: currentUser?.uid // 🔹 Chat único por usuario
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var newMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // 🔹 Escuchar mensajes del usuario actual
    DisposableEffect(userChatId) {
        var registration: ListenerRegistration? = null
        if (userChatId != null) {
            registration = db.collection("chats").document(userChatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, _ ->
                    val list = snapshot?.documents?.map { doc ->
                        Message(
                            id = doc.id,
                            text = doc.getString("text") ?: "",
                            senderId = doc.getString("senderId") ?: "",
                            senderEmail = doc.getString("senderEmail") ?: "",
                            timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                        )
                    }.orEmpty()
                    messages = list
                }
        }
        onDispose { registration?.remove() }
    }

    // 🔹 Enviar mensaje (solo guardado para el usuario actual)
    fun sendMessage() {
        if (newMessage.isNotBlank() && userChatId != null && currentUser != null) {
            val message = hashMapOf(
                "text" to newMessage.trim(),
                "senderId" to currentUser.uid,
                "senderEmail" to (currentUser.email ?: ""),
                "timestamp" to Timestamp.now()
            )
            scope.launch {
                db.collection("chats").document(userChatId)
                    .collection("messages")
                    .add(message)
                newMessage = ""
            }
        }
    }

    Scaffold(
        topBar = {
            // 🔹 Fondo amarillo grande detrás del TopAppBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFEB3B)) // Amarillo brillante
            ) {
                TopAppBar(
                    title = {
                        val displayName = currentUser?.displayName ?: currentUser?.email ?: "Usuario"
                        Text(
                            text = displayName,
                            color = Color.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    actions = {
                        // 🔹 Botón Notificaciones (sin acción)
                        IconButton(onClick = { /* sin acción */ }) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color.Black
                            )
                        }

                        // 🔹 Botón Ajustes
                        IconButton(onClick = { navController.navigate(AppScreens.SettingsScreen.route) }) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = "Ajustes",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent // Fondo transparente para usar el del Box
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp) // 🔹 Aumentar altura
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 🔹 Lista de mensajes
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = true
            ) {
                items(messages) { message ->
                    MessageBubble(message)
                }
            }

            // 🔹 Campo de texto para escribir mensajes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribe un mensaje...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { sendMessage() },
                    enabled = newMessage.isNotBlank() && currentUser != null
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Enviar")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val currentUserId = Firebase.auth.currentUser?.uid
    val isCurrentUser = message.senderId == currentUserId

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            if (!isCurrentUser) {
                Text(
                    text = message.senderEmail.ifEmpty { "Usuario" },
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentUser)
                        Color(0xFF007AFF) else Color(0xFFE5E5EA)
                )
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(16.dp),
                    color = if (isCurrentUser) Color.White else Color.Black
                )
            }
        }
    }
}
