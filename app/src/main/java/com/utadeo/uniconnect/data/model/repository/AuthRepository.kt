package com.utadeo.uniconnect.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.utadeo.uniconnect.data.model.repository.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Iniciar sesión con email y contraseña
     */
    suspend fun loginWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.Success(result.user!!)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Registrar usuario en Firebase Auth y crear documento en Firestore (colección "users")
     */
    suspend fun registerWithEmailAndPassword(
        email: String,
        password: String,
        displayName: String? = null,
        interests: List<String> = emptyList(),
        profilePicture: String = "",
        bio: String = "",
        university: String = "",
        career: String = "",
        semester: Int = 0
    ): Result<FirebaseUser> {
        return try {
            // Crear usuario en Firebase Auth
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Usuario no creado correctamente")

            // Actualizar displayName en Firebase Auth
            if (!displayName.isNullOrEmpty()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            // Crear objeto User con toda la información
            val userData = User(
                uid = user.uid,
                email = email,
                displayName = displayName ?: "",
                profilePicture = profilePicture,
                bio = bio,
                interests = interests,
                university = university,
                career = career,
                semester = semester,
                isEmailVerified = user.isEmailVerified,
                createdAt = System.currentTimeMillis()
            )

            // Guardar en Firestore
            firestore.collection("users")
                .document(user.uid)
                .set(userData.toMap(skipEmptyStrings = false))
                .await()

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Enviar correo de verificación
     */
    suspend fun sendEmailVerification(): Result<Boolean> {
        return try {
            currentUser?.sendEmailVerification()?.await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Obtener el perfil completo del usuario actual desde Firestore
     */
    suspend fun getCurrentUserProfile(): Result<User> {
        return try {
            val user = currentUser ?: throw Exception("No hay usuario autenticado")
            val snapshot = firestore.collection("users").document(user.uid).get().await()
            val data = snapshot.data
            val profile = User.fromMap(data) ?: throw Exception("Perfil no encontrado")
            Result.Success(profile)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Actualizar los datos del perfil del usuario actual en Firestore
     */
    suspend fun updateUserProfile(updates: Map<String, Any?>): Result<Boolean> {
        return try {
            val user = currentUser ?: throw Exception("No hay usuario autenticado")
            firestore.collection("users").document(user.uid).update(updates).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun isEmailVerified(): Boolean {
        return currentUser?.isEmailVerified ?: false
    }
}

/**
 * Wrapper para manejar los resultados de operaciones Firebase
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
