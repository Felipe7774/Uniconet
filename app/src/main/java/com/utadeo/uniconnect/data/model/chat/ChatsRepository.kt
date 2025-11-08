package com.utadeo.uniconnect.data.model.chat

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

data class ChatPreview(
    val chatId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val otherUserEmail: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Timestamp = Timestamp.now(),
    val unreadCount: Int = 0
)

object ChatsRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private const val CHATS_COLLECTION = "individual_chats"
    private const val MESSAGES_SUBCOLLECTION = "messages"

    /**
     * Obtiene o crea un chat entre dos usuarios
     * @return El ID del chat (combinación de UIDs ordenados)
     */
    fun getChatId(userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        return "${sortedIds[0]}_${sortedIds[1]}"
    }

    /**
     * Inicia un chat con otro usuario
     */
    suspend fun startChat(otherUserId: String, otherUserName: String): String {
        val currentUser = auth.currentUser ?: throw Exception("Usuario no autenticado")
        val chatId = getChatId(currentUser.uid, otherUserId)

        val chatData = hashMapOf(
            "participants" to listOf(currentUser.uid, otherUserId),
            "participantsMap" to mapOf(
                currentUser.uid to (currentUser.displayName ?: currentUser.email ?: "Usuario"),
                otherUserId to otherUserName
            ),
            "createdAt" to Timestamp.now(),
            "lastMessage" to "",
            "lastMessageTime" to Timestamp.now()
        )

        // Crear o actualizar el documento del chat
        firestore.collection(CHATS_COLLECTION)
            .document(chatId)
            .set(chatData)
            .await()

        return chatId
    }

    /**
     * Envía un mensaje en un chat
     */
    suspend fun sendMessage(chatId: String, messageText: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            val message = hashMapOf(
                "text" to messageText.trim(),
                "senderId" to currentUser.uid,
                "senderName" to (currentUser.displayName ?: currentUser.email ?: "Usuario"),
                "timestamp" to Timestamp.now()
            )

            // Agregar mensaje a la subcolección
            firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .collection(MESSAGES_SUBCOLLECTION)
                .add(message)
                .await()

            // Actualizar último mensaje del chat
            firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to messageText.trim(),
                        "lastMessageTime" to Timestamp.now()
                    )
                )
                .await()

            // 🔴 NUEVO: Enviar notificación al otro usuario
            try {
                val chatDoc = firestore.collection(CHATS_COLLECTION)
                    .document(chatId)
                    .get()
                    .await()

                val participants = chatDoc.get("participants") as? List<*>
                val otherUserId = participants?.firstOrNull { it != currentUser.uid }?.toString()

                println("🔔 DEBUG - Chat participants: $participants")
                println("🔔 DEBUG - Other user ID: $otherUserId")
                println("🔔 DEBUG - Current user: ${currentUser.uid}")

                if (otherUserId != null && otherUserId != currentUser.uid) {
                    println("🔔 DEBUG - Intentando crear notificación...")

                    val notificationData = hashMapOf(
                        "type" to "NEW_MESSAGE",
                        "userId" to otherUserId,
                        "fromUserId" to currentUser.uid,
                        "fromUserName" to (currentUser.displayName ?: currentUser.email ?: "Usuario"),
                        "fromUserImage" to "",
                        "message" to "${currentUser.displayName ?: currentUser.email ?: "Usuario"} te ha enviado un mensaje",
                        "targetId" to chatId,
                        "targetTitle" to "",
                        "timestamp" to Timestamp.now(),
                        "isRead" to false
                    )

                    println("🔔 DEBUG - Datos de notificación: $notificationData")

                    val result = firestore.collection("notifications")
                        .add(notificationData)
                        .await()

                    println("✅ DEBUG - Notificación creada con ID: ${result.id}")
                } else {
                    println("⚠️ DEBUG - No se envía notificación (mismo usuario o userId nulo)")
                }
            } catch (notifError: Exception) {
                println("❌ ERROR al crear notificación: ${notifError.message}")
                notifError.printStackTrace()
                // No fallar el envío del mensaje aunque falle la notificación
            }

            true
        } catch (e: Exception) {
            println("❌ ERROR en sendMessage: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtiene todos los chats del usuario actual
     */
    suspend fun getMyChats(): List<ChatPreview> {
        val currentUser = auth.currentUser
        println("🔍 DEBUG - Usuario actual: ${currentUser?.uid}")
        println("🔍 DEBUG - Email: ${currentUser?.email}")

        if (currentUser == null) {
            println("❌ DEBUG - No hay usuario autenticado")
            return emptyList()
        }

        return try {
            println("📡 DEBUG - Consultando colección: $CHATS_COLLECTION")
            println("📡 DEBUG - Buscando chats para: ${currentUser.uid}")

            val snapshot = firestore.collection(CHATS_COLLECTION)
                .whereArrayContains("participants", currentUser.uid)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .get()
                .await()

            println("📦 DEBUG - Documentos encontrados: ${snapshot.size()}")
            println("📦 DEBUG - Documentos vacíos: ${snapshot.isEmpty}")

            if (snapshot.isEmpty) {
                println("⚠️ DEBUG - No se encontraron documentos en la consulta")
                return emptyList()
            }

            val chats = snapshot.documents.mapNotNull { doc ->
                println("📄 DEBUG - Procesando documento: ${doc.id}")
                println("📄 DEBUG - Datos: ${doc.data}")

                val participants = doc.get("participants") as? List<*>
                println("👥 DEBUG - Participants: $participants")

                if (participants == null) {
                    println("❌ DEBUG - No hay participants en el documento ${doc.id}")
                    return@mapNotNull null
                }

                val participantsMap = doc.get("participantsMap") as? Map<*, *>
                println("🗺️ DEBUG - ParticipantsMap: $participantsMap")

                if (participantsMap == null) {
                    println("❌ DEBUG - No hay participantsMap en el documento ${doc.id}")
                    return@mapNotNull null
                }

                // Encontrar al otro usuario
                val otherUserId = participants.firstOrNull { it != currentUser.uid }?.toString()
                println("👤 DEBUG - OtherUserId: $otherUserId")

                if (otherUserId == null) {
                    println("❌ DEBUG - No se encontró otro usuario en ${doc.id}")
                    return@mapNotNull null
                }

                val otherUserName = participantsMap[otherUserId]?.toString() ?: "Usuario"
                println("✅ DEBUG - Chat válido encontrado: $otherUserName")

                ChatPreview(
                    chatId = doc.id,
                    otherUserId = otherUserId,
                    otherUserName = otherUserName,
                    otherUserEmail = "",
                    lastMessage = doc.getString("lastMessage") ?: "",
                    lastMessageTime = doc.getTimestamp("lastMessageTime") ?: Timestamp.now(),
                    unreadCount = 0
                )
            }

            println("✅ DEBUG - Total de chats procesados: ${chats.size}")
            chats

        } catch (e: Exception) {
            println("💥 DEBUG - Error al obtener chats: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Escucha mensajes de un chat en tiempo real (Flow)
     */
    fun getChatMessagesFlow(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = firestore.collection(CHATS_COLLECTION)
            .document(chatId)
            .collection(MESSAGES_SUBCOLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    ChatMessage(
                        id = doc.id,
                        text = doc.getString("text") ?: "",
                        senderId = doc.getString("senderId") ?: "",
                        senderName = doc.getString("senderName") ?: "Usuario",
                        timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                    )
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Verifica si existe un chat entre dos usuarios
     */
    suspend fun chatExists(userId1: String, userId2: String): Boolean {
        val chatId = getChatId(userId1, userId2)
        return try {
            val doc = firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    // 🔽🔽🔽 FUNCIÓN REEMPLAZADA 🔽🔽🔽
    suspend fun getUserChatsFlow(userId: String): Flow<List<ChatPreview>> = callbackFlow {
        val listener = firestore.collection(CHATS_COLLECTION)  // ✅ Usar "individual_chats"
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val chatsList = snapshot.documents.mapNotNull { doc ->
                    val participants = doc.get("participants") as? List<*>
                    val participantsMap = doc.get("participantsMap") as? Map<*, *>

                    if (participants == null || participantsMap == null) {
                        return@mapNotNull null
                    }

                    // Encontrar al otro usuario
                    val otherUserId = participants.firstOrNull { it != userId }?.toString()
                        ?: return@mapNotNull null

                    val otherUserName = participantsMap[otherUserId]?.toString() ?: "Usuario"
                    val lastMessage = doc.getString("lastMessage") ?: "Sin mensajes"
                    val lastMessageTime = doc.getTimestamp("lastMessageTime") ?: Timestamp.now()

                    ChatPreview(
                        chatId = doc.id,
                        otherUserId = otherUserId,
                        otherUserName = otherUserName,
                        otherUserEmail = "",
                        lastMessage = lastMessage,
                        lastMessageTime = lastMessageTime,
                        unreadCount = 0
                    )
                }

                trySend(chatsList)
            }

        awaitClose { listener.remove() }
    }
}
