package com.utadeo.uniconnect.data.model.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.uniconnect.R
import com.utadeo.uniconnect.data.model.chat.ChatMessage
import com.utadeo.uniconnect.data.model.chat.ChatsRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private val YellowPrimary = Color(0xFFFDD835)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualChatScreen(
    navController: NavController,
    chatId: String,
    otherUserId: String,
    otherUserName: String
) {
    val poppins = FontFamily(Font(R.font.poppins_regular))
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var newMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Escuchar mensajes en tiempo real
    LaunchedEffect(chatId) {
        ChatsRepository.getChatMessagesFlow(chatId).collect { newMessages ->
            messages = newMessages

            // Auto-scroll al último mensaje
            if (newMessages.isNotEmpty()) {
                scope.launch {
                    listState.animateScrollToItem(newMessages.size - 1)
                }
            }
        }
    }

    fun sendMessage() {
        if (newMessage.isBlank() || isSending) return

        isSending = true
        val messageToSend = newMessage
        newMessage = ""

        scope.launch {
            val success = ChatsRepository.sendMessage(chatId, messageToSend)
            isSending = false

            if (!success) {
                // Restaurar mensaje si falla
                newMessage = messageToSend
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = otherUserName,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("user_detail/$otherUserId")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Ver perfil",
                            tint = Color.Black
                        )
                    }
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
            // Lista de mensajes
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👋", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Inicia la conversación",
                                    fontFamily = poppins,
                                    fontSize = 16.sp,
                                    color = Color.Black.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                } else {
                    items(messages) { message ->
                        MessageBubble(
                            message = message,
                            isFromCurrentUser = message.senderId == currentUserId,
                            poppins = poppins
                        )
                    }
                }
            }

            // Campo de texto para enviar mensajes
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Escribe un mensaje...",
                                fontFamily = poppins,
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = poppins,
                            fontSize = 14.sp
                        )
                    )

                    IconButton(
                        onClick = { sendMessage() },
                        enabled = newMessage.isNotBlank() && !isSending,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (newMessage.isNotBlank() && !isSending)
                                    Color.Black
                                else
                                    Color.Gray
                            )
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isFromCurrentUser: Boolean,
    poppins: FontFamily
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                    bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
                ),
                color = if (isFromCurrentUser) Color.Black else Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    color = if (isFromCurrentUser) Color.White else Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatMessageTime(message.timestamp),
                fontFamily = poppins,
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

private fun formatMessageTime(timestamp: com.google.firebase.Timestamp): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}