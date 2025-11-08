package com.utadeo.uniconnect.data.model.repository

/**
 * Modelo de usuario guardado en Firestore en la colección "users".
 *
 * Mantén los nombres de las propiedades exactamente iguales que en Firestore para que
 * la serialización/deserialización automática funcione correctamente.
 */
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val profilePicture: String = "",        // URL pública de la foto (Cloudinary o Firebase Storage)
    val bio: String = "",                   // Descripción / presentación del usuario
    val interests: List<String> = emptyList(),
    val university: String = "",
    val career: String = "",
    val semester: Int = 0,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Convierte el modelo a mapa — útil para actualizaciones parciales en Firestore.
     * Omite valores vacíos si quieres evitar sobreescribir con strings vacíos.
     */
    fun toMap(skipEmptyStrings: Boolean = true): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "profilePicture" to profilePicture,
            "bio" to bio,
            "interests" to interests,
            "university" to university,
            "career" to career,
            "semester" to semester,
            "isEmailVerified" to isEmailVerified,
            "createdAt" to createdAt
        )

        if (skipEmptyStrings) {
            // elimina keys con string vacío para no sobreescribir en Firestore
            val keysToRemove = map.filterValues { v ->
                (v is String && v.isBlank())
            }.keys
            keysToRemove.forEach { map.remove(it) }
        }

        return map
    }

    companion object {
        /**
         * Construye un User a partir de un Map (por ejemplo, un DocumentSnapshot.data).
         * Retorna null si el map es nulo o no contiene uid/email.
         */
        fun fromMap(map: Map<String, Any?>?): User? {
            if (map == null) return null
            val uid = map["uid"] as? String ?: ""
            val email = map["email"] as? String ?: ""
            val displayName = map["displayName"] as? String ?: ""
            val profilePicture = map["profilePicture"] as? String ?: ""
            val bio = map["bio"] as? String ?: ""
            val interestsAny = map["interests"]
            val interests: List<String> = when (interestsAny) {
                is List<*> -> interestsAny.filterIsInstance<String>()
                else -> emptyList()
            }
            val university = map["university"] as? String ?: ""
            val career = map["career"] as? String ?: ""
            val semester = (map["semester"] as? Long)?.toInt()
                ?: (map["semester"] as? Int) ?: 0
            val isEmailVerified = map["isEmailVerified"] as? Boolean ?: false
            val createdAt = when (val v = map["createdAt"]) {
                is Long -> v
                is Number -> v.toLong()
                else -> System.currentTimeMillis()
            }

            return User(
                uid = uid,
                email = email,
                displayName = displayName,
                profilePicture = profilePicture,
                bio = bio,
                interests = interests,
                university = university,
                career = career,
                semester = semester,
                isEmailVerified = isEmailVerified,
                createdAt = createdAt
            )
        }
    }
}
