package com.utadeo.uniconnect.data.manager

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.utadeo.uniconnect.data.model.ui.Login.CloudinaryUploader
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

/**
 * Gestor de registro temporal para UniConnect
 * Guarda todos los datos localmente hasta que el usuario complete el registro
 */
class RegistrationManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("uniconnect_registration", Context.MODE_PRIVATE)
    private val tempDir = File(context.cacheDir, "registration_temp")

    companion object {
        private const val TAG = "RegistrationManager"
    }

    init {
        if (!tempDir.exists()) {
            tempDir.mkdirs()
            Log.d(TAG, "Directorio temporal creado: ${tempDir.absolutePath}")
        }
    }

    // ============ DATA CLASS ============

    data class TempRegistrationData(
        val email: String? = null,
        val password: String? = null,
        val verificationImagePath: String? = null,  // Foto de verificación
        val nombre: String? = null,
        val profileImagePath: String? = null,       // Foto de perfil
        val biografia: String? = null,
        val intereses: List<String> = emptyList(),
        val currentStep: Int = 0
    )

    // ============ GUARDAR DATOS TEMPORALES ============

    // PASO 1: Email
    fun saveEmail(email: String) {
        prefs.edit().putString("email", email).apply()
        saveStep(1)
        Log.d(TAG, "✅ Email guardado: $email")
    }

    // PASO 2: Password
    fun savePassword(password: String) {
        prefs.edit().putString("password", password).apply()
        saveStep(2)
        Log.d(TAG, "✅ Password guardado")
    }

    // PASO 4: Foto de Verificación (guarda archivo local, NO sube)
    fun saveVerificationImage(bitmap: Bitmap): String {
        val file = File(tempDir, "verification_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        val path = file.absolutePath
        prefs.edit().putString("verification_image_path", path).apply()
        saveStep(4)
        Log.d(TAG, "✅ Foto verificación guardada: $path")
        return path
    }

    // PASO 5: Nombre
    fun saveNombre(nombre: String) {
        prefs.edit().putString("nombre", nombre).apply()
        saveStep(5)
        Log.d(TAG, "✅ Nombre guardado: $nombre")
    }

    // PASO 6: Foto de Perfil (guarda URI/Bitmap local, NO sube)
    fun saveProfileImage(uri: Uri): String {
        val file = File(tempDir, "profile_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        val path = file.absolutePath
        prefs.edit().putString("profile_image_path", path).apply()
        saveStep(6)
        Log.d(TAG, "✅ Foto perfil guardada: $path")
        return path
    }

    fun saveProfileImageFromBitmap(bitmap: Bitmap): String {
        val file = File(tempDir, "profile_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        val path = file.absolutePath
        prefs.edit().putString("profile_image_path", path).apply()
        saveStep(6)
        Log.d(TAG, "✅ Foto perfil guardada desde bitmap: $path")
        return path
    }

    // PASO 7: Biografía
    fun saveBiografia(bio: String) {
        prefs.edit().putString("biografia", bio).apply()
        saveStep(7)
        Log.d(TAG, "✅ Biografía guardada")
    }

    // PASO 8: Intereses
    fun saveIntereses(intereses: List<String>) {
        prefs.edit().putString("intereses", intereses.joinToString(",")).apply()
        saveStep(8)
        Log.d(TAG, "✅ Intereses guardados: $intereses")
    }

    private fun saveStep(step: Int) {
        prefs.edit().putInt("current_step", step).apply()
    }

    // ============ OBTENER DATOS TEMPORALES ============

    fun getTempData(): TempRegistrationData {
        val interesesStr = prefs.getString("intereses", "") ?: ""
        val interesesList = if (interesesStr.isNotEmpty()) {
            interesesStr.split(",")
        } else {
            emptyList()
        }

        return TempRegistrationData(
            email = prefs.getString("email", null),
            password = prefs.getString("password", null),
            verificationImagePath = prefs.getString("verification_image_path", null),
            nombre = prefs.getString("nombre", null),
            profileImagePath = prefs.getString("profile_image_path", null),
            biografia = prefs.getString("biografia", null),
            intereses = interesesList,
            currentStep = prefs.getInt("current_step", 0)
        )
    }

    fun hasIncompleteRegistration(): Boolean {
        val step = prefs.getInt("current_step", 0)
        return step > 0 && step < 9
    }

    fun getCurrentStep(): Int {
        return prefs.getInt("current_step", 0)
    }

    // ============ COMPLETAR REGISTRO (PASO 9) ============

    suspend fun completeRegistration(
        onProgress: (String) -> Unit
    ): Result<String> {
        return try {
            val data = getTempData()

            Log.d(TAG, "🚀 Iniciando registro completo...")

            // Validaciones
            if (data.email == null || data.password == null) {
                throw Exception("Falta email o contraseña")
            }
            if (data.verificationImagePath == null) {
                throw Exception("Falta foto de verificación")
            }
            if (data.nombre == null) {
                throw Exception("Falta nombre")
            }

            onProgress("Creando tu cuenta...")
            Log.d(TAG, "📧 Creando cuenta con email: ${data.email}")

            // 1. CREAR USUARIO EN FIREBASE AUTH
            val auth = FirebaseAuth.getInstance()
            val authResult = auth.createUserWithEmailAndPassword(data.email, data.password).await()
            val userId = authResult.user?.uid ?: throw Exception("Error al crear usuario")

            Log.d(TAG, "✅ Cuenta creada con UID: $userId")

            // 2. SUBIR FOTO DE VERIFICACIÓN
            onProgress("Subiendo foto de verificación...")
            val verificationFile = File(data.verificationImagePath)
            val verificationUrl = uploadImageToCloudinary(verificationFile)
            Log.d(TAG, "✅ Foto verificación subida: $verificationUrl")

            // 3. SUBIR FOTO DE PERFIL (si existe)
            var profileUrl: String? = null
            if (data.profileImagePath != null) {
                onProgress("Subiendo foto de perfil...")
                val profileFile = File(data.profileImagePath)
                if (profileFile.exists()) {
                    profileUrl = uploadImageToCloudinary(profileFile)
                    Log.d(TAG, "✅ Foto perfil subida: $profileUrl")
                }
            }

            // 4. GUARDAR TODO EN FIRESTORE
            onProgress("Guardando tu información...")
            val firestore = FirebaseFirestore.getInstance()

            val userData = hashMapOf(
                "email" to data.email,
                "nombre" to data.nombre,
                "biografia" to (data.biografia ?: ""),
                "intereses" to data.intereses,
                "verificationImageUrl" to verificationUrl,
                "photoUrl" to (profileUrl ?: ""),
                "verified" to false,
                "registrationCompleted" to true,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()

            Log.d(TAG, "✅ Datos guardados en Firestore")

            // 5. ENVIAR EMAIL DE VERIFICACIÓN
            try {
                auth.currentUser?.sendEmailVerification()?.await()
                Log.d(TAG, "✅ Email de verificación enviado")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ No se pudo enviar email de verificación: ${e.message}")
            }

            onProgress("¡Registro completado!")

            // 6. LIMPIAR DATOS TEMPORALES
            clearTempData()

            Result.success(userId)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al completar registro: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun uploadImageToCloudinary(file: File): String {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            CloudinaryUploader.uploadImage(
                file,
                onSuccess = { url ->
                    continuation.resume(url) {}
                },
                onError = { error ->
                    continuation.cancel(Exception("Error al subir imagen: $error"))
                }
            )
        }
    }

    // ============ LIMPIAR DATOS ============

    fun clearTempData() {
        Log.d(TAG, "🧹 Limpiando datos temporales...")

        tempDir.listFiles()?.forEach { file ->
            file.delete()
            Log.d(TAG, "🗑️ Archivo eliminado: ${file.name}")
        }

        prefs.edit().clear().apply()

        Log.d(TAG, "✅ Datos temporales limpiados")
    }

    suspend fun cancelRegistration() {
        Log.d(TAG, "❌ Cancelando registro...")

        try {
            val auth = FirebaseAuth.getInstance()
            auth.currentUser?.delete()?.await()
            auth.signOut()
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo eliminar usuario: ${e.message}")
        }

        clearTempData()
    }

    // ===================== CÓDIGO AGREGADO =====================

    // En tu RegistrationManager, cuando guardes los datos del usuario en Firestore,
    // asegúrate de guardar AMBOS campos: "nombre" Y "displayName" para compatibilidad

    // Busca donde guardas los datos del usuario y reemplázalo por esto:

    fun guardarDatosUsuario(
        userId: String,
        nombre: String,
        biografia: String,
        intereses: List<String>,
        photoUrl: String? = null
    ) {
        val userData = hashMapOf(
            "nombre" to nombre,              // ✅ Para tu app
            "displayName" to nombre,         // ✅ Para compatibilidad
            "biografia" to biografia,
            "bio" to biografia,              // ✅ Para compatibilidad
            "intereses" to intereses,
            "interests" to intereses,        // ✅ Para compatibilidad
            "photoUrl" to (photoUrl ?: ""),
            "createdAt" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                val user = FirebaseAuth.getInstance().currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(nombre)
                    .build()
                user?.updateProfile(profileUpdates)
            }
    }

    // Si ya tienes usuarios registrados, puedes crear una función de migración:

    suspend fun migrarUsuariosExistentes() {
        val firestore = FirebaseFirestore.getInstance()
        val snapshot = firestore.collection("users").get().await()

        snapshot.documents.forEach { doc ->
            val updates = hashMapOf<String, Any>()

            doc.getString("nombre")?.let {
                if (!doc.contains("displayName")) {
                    updates["displayName"] = it
                }
            }

            doc.getString("biografia")?.let {
                if (!doc.contains("bio")) {
                    updates["bio"] = it
                }
            }

            (doc.get("intereses") as? List<*>)?.let {
                if (!doc.contains("interests")) {
                    updates["interests"] = it
                }
            }

            if (updates.isNotEmpty()) {
                doc.reference.update(updates)
            }
        }
    }
}
