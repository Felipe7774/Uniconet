package com.utadeo.uniconnect.data.model.ui.Login.notifications

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Tipos de notificaciones soportadas
 */
enum class NotificationType {
    NEW_MESSAGE,           // Nuevo mensaje en chat
    NEW_PARTICIPANT,       // Nueva persona se unió a actividad
    NEW_ANSWER            // Nueva respuesta a pregunta
}

/**
 * Modelo de notificación
 */
data class AppNotification(
    val id: String = "",
    val type: NotificationType = NotificationType.NEW_MESSAGE,
    val userId: String = "",           // A quién va dirigida
    val fromUserId: String = "",       // Quién la generó
    val fromUserName: String = "",
    val fromUserImage: String = "",
    val message: String = "",
    val targetId: String = "",         // ID del chat/actividad/pregunta
    val targetTitle: String = "",      // Título de la actividad/pregunta
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)

object NotificationsRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private const val NOTIFICATIONS_COLLECTION = "notifications"
    private const val TAG = "NotificationsRepo"

    /**
     * 🔴 Escucha notificaciones del usuario actual en tiempo real (CORREGIDO)
     */
    fun getUserNotificationsFlow(): Flow<List<AppNotification>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid

        Log.d(TAG, "🔍 Iniciando listener de notificaciones para usuario: $currentUserId")

        if (currentUserId == null) {
            Log.w(TAG, "⚠️ No hay usuario autenticado")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("userId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error al escuchar notificaciones: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w(TAG, "⚠️ Snapshot es null")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                Log.d(TAG, "📦 Recibidos ${snapshot.size()} documentos de notificaciones")

                if (snapshot.isEmpty) {
                    Log.d(TAG, "📭 No hay notificaciones para este usuario")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val notifications = mutableListOf<AppNotification>()

                snapshot.documents.forEach { doc ->
                    try {
                        Log.d(TAG, "📄 Procesando documento: ${doc.id}")
                        Log.d(TAG, "   Datos: ${doc.data}")

                        val typeString = doc.getString("type")
                        Log.d(TAG, "   Type string: $typeString")

                        if (typeString == null) {
                            Log.w(TAG, "⚠️ Documento ${doc.id} no tiene campo 'type'")
                            return@forEach
                        }

                        // 🔴 CORRECCIÓN: Manejo robusto de tipos
                        val type = try {
                            NotificationType.valueOf(typeString)
                        } catch (e: IllegalArgumentException) {
                            Log.e(TAG, "❌ Tipo inválido '$typeString' en doc ${doc.id}")
                            return@forEach
                        }

                        val notification = AppNotification(
                            id = doc.id,
                            type = type,
                            userId = doc.getString("userId") ?: "",
                            fromUserId = doc.getString("fromUserId") ?: "",
                            fromUserName = doc.getString("fromUserName") ?: "Usuario",
                            fromUserImage = doc.getString("fromUserImage") ?: "",
                            message = doc.getString("message") ?: "",
                            targetId = doc.getString("targetId") ?: "",
                            targetTitle = doc.getString("targetTitle") ?: "",
                            timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                            isRead = doc.getBoolean("isRead") ?: false
                        )

                        Log.d(TAG, "✅ Notificación parseada: ${notification.message}")
                        notifications.add(notification)

                    } catch (e: Exception) {
                        Log.e(TAG, "💥 Error al parsear notificación ${doc.id}: ${e.message}")
                        e.printStackTrace()
                    }
                }

                Log.d(TAG, "📬 Total notificaciones válidas: ${notifications.size}")
                trySend(notifications)
            }

        awaitClose {
            Log.d(TAG, "🔌 Cerrando listener de notificaciones")
            listener.remove()
        }
    }

    /**
     * 🔢 Cuenta notificaciones no leídas en tiempo real
     */
    fun getUnreadCountFlow(): Flow<Int> = callbackFlow {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error al contar no leídas: ${error.message}")
                    trySend(0)
                    return@addSnapshotListener
                }

                val count = snapshot?.size() ?: 0
                Log.d(TAG, "🔢 Notificaciones no leídas: $count")
                trySend(count)
            }

        awaitClose { listener.remove() }
    }

    /**
     * 📨 Crear notificación de nuevo mensaje
     */
    suspend fun createMessageNotification(
        toUserId: String,
        chatId: String,
        fromUserName: String,
        fromUserImage: String = ""
    ): Boolean {
        val currentUser = auth.currentUser ?: return false

        // No notificar si el mensaje es del mismo usuario
        if (currentUser.uid == toUserId) return false

        return try {
            val notification = hashMapOf(
                "type" to "NEW_MESSAGE",  // 🔴 String literal, no enum
                "userId" to toUserId,
                "fromUserId" to currentUser.uid,
                "fromUserName" to fromUserName,
                "fromUserImage" to fromUserImage,
                "message" to "$fromUserName te ha enviado un mensaje",
                "targetId" to chatId,
                "targetTitle" to "",
                "timestamp" to Timestamp.now(),
                "isRead" to false
            )

            Log.d(TAG, "📤 Creando notificación de mensaje: $notification")

            firestore.collection(NOTIFICATIONS_COLLECTION)
                .add(notification)
                .await()

            Log.d(TAG, "✅ Notificación de mensaje creada")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al crear notificación: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * 👥 Crear notificación de nuevo participante
     */
    suspend fun createParticipantNotification(
        activityCreatorId: String,
        activityId: String,
        activityTitle: String,
        participantName: String,
        participantImage: String = ""
    ): Boolean {
        val currentUser = auth.currentUser ?: return false

        // No notificar si el creador es el mismo que se une
        if (currentUser.uid == activityCreatorId) return false

        return try {
            val notification = hashMapOf(
                "type" to "NEW_PARTICIPANT",  // 🔴 String literal
                "userId" to activityCreatorId,
                "fromUserId" to currentUser.uid,
                "fromUserName" to participantName,
                "fromUserImage" to participantImage,
                "message" to "$participantName se ha unido a tu actividad",
                "targetId" to activityId,
                "targetTitle" to activityTitle,
                "timestamp" to Timestamp.now(),
                "isRead" to false
            )

            Log.d(TAG, "📤 Creando notificación de participante: $notification")

            firestore.collection(NOTIFICATIONS_COLLECTION)
                .add(notification)
                .await()

            Log.d(TAG, "✅ Notificación de participante creada")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al crear notificación: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * ❓ Crear notificación de nueva respuesta
     */
    suspend fun createAnswerNotification(
        questionCreatorId: String,
        questionId: String,
        questionText: String,
        answererName: String,
        answererImage: String = ""
    ): Boolean {
        val currentUser = auth.currentUser ?: return false

        // No notificar si el creador responde su propia pregunta
        if (currentUser.uid == questionCreatorId) return false

        return try {
            val notification = hashMapOf(
                "type" to "NEW_ANSWER",  // 🔴 String literal
                "userId" to questionCreatorId,
                "fromUserId" to currentUser.uid,
                "fromUserName" to answererName,
                "fromUserImage" to answererImage,
                "message" to "$answererName respondió tu pregunta",
                "targetId" to questionId,
                "targetTitle" to questionText,
                "timestamp" to Timestamp.now(),
                "isRead" to false
            )

            Log.d(TAG, "📤 Creando notificación de respuesta: $notification")

            firestore.collection(NOTIFICATIONS_COLLECTION)
                .add(notification)
                .await()

            Log.d(TAG, "✅ Notificación de respuesta creada")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al crear notificación: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * ✅ Marcar notificación como leída
     */
    suspend fun markAsRead(notificationId: String): Boolean {
        return try {
            firestore.collection(NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .update("isRead", true)
                .await()
            Log.d(TAG, "✅ Notificación $notificationId marcada como leída")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al marcar como leída: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * ✅ Marcar todas las notificaciones como leídas
     */
    suspend fun markAllAsRead(): Boolean {
        val currentUserId = auth.currentUser?.uid ?: return false

        return try {
            val snapshot = firestore.collection(NOTIFICATIONS_COLLECTION)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()

            Log.d(TAG, "✅ Todas las notificaciones marcadas como leídas")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al marcar todas como leídas: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * 🗑️ Eliminar notificación
     */
    suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            firestore.collection(NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .delete()
                .await()
            Log.d(TAG, "✅ Notificación $notificationId eliminada")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}