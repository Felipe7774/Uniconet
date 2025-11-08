package com.utadeo.uniconnect.data.model.activity

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

object QuestionsRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 📚 Nombre de la colección principal y subcolección
    private const val COLLECTION_NAME = "questions"
    private const val RESPONSES_SUBCOL = "responses"

    /**
     * 🟨 Guarda una nueva pregunta en Firestore con el nombre del usuario autenticado.
     */
    suspend fun addQuestion(questionText: String) {
        val currentUser = auth.currentUser
        val userName = currentUser?.displayName ?: currentUser?.email ?: "Usuario anónimo"
        val userId = currentUser?.uid ?: "unknown_user"

        if (questionText.isNotBlank()) {
            val questionData = mapOf(
                "text" to questionText.trim(),
                "userName" to userName,
                "userId" to userId,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_NAME).add(questionData).await()
        }
    }

    /**
     * 🟦 Obtiene todas las preguntas guardadas en Firestore, ordenadas por fecha.
     * Incluye el ID de cada documento para poder agregar respuestas.
     */
    suspend fun getQuestions(): List<Map<String, Any>> {
        val snapshot = firestore.collection(COLLECTION_NAME)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            val mapWithId: MutableMap<String, Any> = HashMap(data)
            mapWithId["id"] = doc.id
            mapWithId
        }
    }

    /**
     * 🟪 Obtiene todas las preguntas con el contador de respuestas
     */
    suspend fun getQuestionsWithResponseCount(): List<Map<String, Any>> {
        val questions = getQuestions()

        return questions.map { question ->
            val questionId = question["id"] as? String ?: return@map question
            val responsesCount = getResponsesCount(questionId)

            question.toMutableMap().apply {
                put("responsesCount", responsesCount)
            }
        }
    }

    /**
     * 🔍 Obtiene una pregunta específica por su ID
     */
    suspend fun getQuestionById(questionId: String): Map<String, Any>? {
        return try {
            val doc = firestore.collection(COLLECTION_NAME)
                .document(questionId)
                .get()
                .await()

            val data = doc.data?.plus("id" to doc.id)?.toMutableMap()

            // Agregar número de respuestas
            data?.put("responsesCount", getResponsesCount(questionId))

            data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 🟩 Agrega una respuesta (con el nombre y userId del usuario autenticado)
     * a la subcolección "responses" dentro de una pregunta específica.
     */
    suspend fun addResponse(questionId: String, responseText: String) {
        val currentUser = auth.currentUser
        val userName = currentUser?.displayName ?: currentUser?.email ?: "Usuario anónimo"
        val userId = currentUser?.uid ?: "unknown_user"

        if (responseText.isNotBlank()) {
            val responseData = mapOf(
                "text" to responseText.trim(),
                "userName" to userName,
                "userId" to userId,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_NAME)
                .document(questionId)
                .collection(RESPONSES_SUBCOL)
                .add(responseData)
                .await()
        }
    }

    /**
     * 🟩 Nueva función con notificación al creador
     */
    suspend fun answerQuestion(questionId: String, answerText: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            // Obtener datos del usuario
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            val userName = userDoc.getString("displayName")
                ?: currentUser.displayName
                ?: currentUser.email
                ?: "Usuario"

            // Obtener datos de la pregunta
            val questionDoc = firestore.collection("questions")
                .document(questionId)
                .get()
                .await()

            val creatorId = questionDoc.getString("userId") ?: return false
            val questionText = questionDoc.getString("text") ?: "Pregunta"

            // Guardar respuesta
            firestore.collection("questions")
                .document(questionId)
                .collection("answers")
                .add(
                    mapOf(
                        "text" to answerText,
                        "userId" to currentUser.uid,
                        "userName" to userName,
                        "timestamp" to Timestamp.now()
                    )
                )
                .await()

            // 🔴 NUEVO: Enviar notificación
            if (creatorId != currentUser.uid) {
                try {
                    val notificationData = hashMapOf(
                        "type" to "NEW_ANSWER",
                        "userId" to creatorId,
                        "fromUserId" to currentUser.uid,
                        "fromUserName" to userName,
                        "fromUserImage" to "",
                        "message" to "$userName respondió tu pregunta",
                        "targetId" to questionId,
                        "targetTitle" to questionText,
                        "timestamp" to Timestamp.now(),
                        "isRead" to false
                    )

                    firestore.collection("notifications")
                        .add(notificationData)
                        .await()

                    println("✅ Notificación de respuesta enviada")
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
     * 🟧 Obtiene todas las respuestas de una pregunta (subcolección "responses"),
     * ordenadas por fecha y con el ID de cada documento incluido.
     */
    suspend fun getResponses(questionId: String): List<Map<String, Any>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_NAME)
                .document(questionId)
                .collection(RESPONSES_SUBCOL)
                .orderBy("timestamp")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val mapWithId: MutableMap<String, Any> = HashMap(data)
                mapWithId["id"] = doc.id
                mapWithId
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 🔢 Obtiene el número de respuestas de una pregunta
     */
    private suspend fun getResponsesCount(questionId: String): Int {
        return try {
            val snapshot = firestore.collection(COLLECTION_NAME)
                .document(questionId)
                .collection(RESPONSES_SUBCOL)
                .get()
                .await()

            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 🎯 Verificar si el usuario actual ha respondido una pregunta
     */
    suspend fun hasUserResponded(questionId: String): Boolean {
        val currentUser = auth.currentUser ?: return false
        val userId = currentUser.uid

        return try {
            val snapshot = firestore.collection(COLLECTION_NAME)
                .document(questionId)
                .collection(RESPONSES_SUBCOL)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 🗑️ Eliminar una respuesta (solo el autor puede eliminarla)
     */
    suspend fun deleteResponse(questionId: String, responseId: String): Boolean {
        return try {
            firestore.collection(COLLECTION_NAME)
                .document(questionId)
                .collection(RESPONSES_SUBCOL)
                .document(responseId)
                .delete()
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
