package com.utadeo.uniconnect.data.model.activity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

object ActivitiesRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private const val COLLECTION_NAME = "activities"
    private const val PARTICIPANTS_SUBCOL = "participants"

    /**
     * 🟢 Crea una nueva actividad en Firestore
     */
    suspend fun createActivity(
        title: String,
        description: String,
        date: String,
        time: String,
        hasBudget: Boolean,
        budgetAmount: Int?,
        locationLat: Double?,
        locationLng: Double?,
        locationName: String?
    ): Boolean {
        val currentUser = auth.currentUser
        val creatorName = currentUser?.displayName ?: currentUser?.email ?: "Usuario Anónimo"
        val creatorId = currentUser?.uid ?: "unknown_user"

        val data = hashMapOf(
            "title" to title,
            "description" to description,
            "date" to date,
            "time" to time,
            "hasBudget" to hasBudget,
            "budgetAmount" to (budgetAmount ?: 0),
            "locationLat" to locationLat,
            "locationLng" to locationLng,
            "locationName" to locationName,
            "creatorName" to creatorName,
            "creatorId" to creatorId,
            "timestamp" to System.currentTimeMillis()
        )

        return try {
            val docRef = firestore.collection(COLLECTION_NAME).add(data).await()

            // Agregar al creador automáticamente como participante
            val participantData = hashMapOf(
                "userId" to creatorId,
                "userName" to creatorName,
                "joinedAt" to System.currentTimeMillis()
            )
            docRef.collection(PARTICIPANTS_SUBCOL).document(creatorId).set(participantData).await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 🔵 Obtiene todas las actividades de Firestore (ordenadas por fecha de creación)
     */
    suspend fun getAllActivities(): List<Map<String, Any>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_NAME)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val mapWithId = HashMap(data)
                mapWithId["id"] = doc.id

                // Obtener número de participantes
                val participantsCount = getParticipantsCount(doc.id)
                mapWithId["participantsCount"] = participantsCount

                mapWithId
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 🟣 Obtiene una actividad específica por su ID
     */
    suspend fun getActivityById(activityId: String): Map<String, Any>? {
        return try {
            val doc = firestore.collection(COLLECTION_NAME).document(activityId).get().await()
            val data = doc.data?.plus("id" to doc.id)?.toMutableMap()

            // Agregar número de participantes
            data?.put("participantsCount", getParticipantsCount(activityId))

            data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 🟡 Unirse a una actividad
     */
    suspend fun joinActivity(activityId: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            // Obtener datos del usuario actual
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            val userName = userDoc.getString("displayName")
                ?: currentUser.displayName
                ?: currentUser.email
                ?: "Usuario"

            // Obtener datos de la actividad
            val activityDoc = firestore.collection("activities")
                .document(activityId)
                .get()
                .await()

            val creatorId = activityDoc.getString("creatorId") ?: return false
            val activityTitle = activityDoc.getString("title") ?: "Actividad"

            // Agregar participante
            firestore.collection("activities")
                .document(activityId)
                .collection("participants")
                .document(currentUser.uid)
                .set(
                    mapOf(
                        "userId" to currentUser.uid,
                        "userName" to userName,
                        "joinedAt" to Timestamp.now()
                    )
                )
                .await()

            // 🔴 NUEVO: Enviar notificación
            if (creatorId != currentUser.uid) {
                try {
                    val notificationData = hashMapOf(
                        "type" to "NEW_PARTICIPANT",
                        "userId" to creatorId,
                        "fromUserId" to currentUser.uid,
                        "fromUserName" to userName,
                        "fromUserImage" to "",
                        "message" to "$userName se ha unido a tu actividad",
                        "targetId" to activityId,
                        "targetTitle" to activityTitle,
                        "timestamp" to Timestamp.now(),
                        "isRead" to false
                    )

                    firestore.collection("notifications")
                        .add(notificationData)
                        .await()

                    println("✅ Notificación de participante enviada")
                } catch (e: Exception) {
                    println("❌ Error al crear notificación: ${e.message}")
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 🔴 Salir de una actividad
     */
    suspend fun leaveActivity(activityId: String): Boolean {
        val currentUser = auth.currentUser ?: return false
        val userId = currentUser.uid

        return try {
            firestore.collection(COLLECTION_NAME)
                .document(activityId)
                .collection(PARTICIPANTS_SUBCOL)
                .document(userId)
                .delete()
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 🟠 Verificar si el usuario actual está participando
     */
    suspend fun isUserParticipating(activityId: String): Boolean {
        val currentUser = auth.currentUser ?: return false
        val userId = currentUser.uid

        return try {
            val doc = firestore.collection(COLLECTION_NAME)
                .document(activityId)
                .collection(PARTICIPANTS_SUBCOL)
                .document(userId)
                .get()
                .await()

            doc.exists()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 📊 Obtener participantes de una actividad
     */
    suspend fun getParticipants(activityId: String): List<Map<String, Any>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_NAME)
                .document(activityId)
                .collection(PARTICIPANTS_SUBCOL)
                .orderBy("joinedAt")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.plus("id" to doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 🔢 Obtener número de participantes
     */
    private suspend fun getParticipantsCount(activityId: String): Int {
        return try {
            val snapshot = firestore.collection(COLLECTION_NAME)
                .document(activityId)
                .collection(PARTICIPANTS_SUBCOL)
                .get()
                .await()

            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 🎯 Obtener actividades en las que participa el usuario actual
     */
    suspend fun getMyActivities(): List<Map<String, Any>> {
        val currentUser = auth.currentUser ?: return emptyList()
        val userId = currentUser.uid

        return try {
            val allActivities = getAllActivities()

            allActivities.filter { activity ->
                val activityId = activity["id"] as? String ?: return@filter false
                isUserParticipating(activityId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
