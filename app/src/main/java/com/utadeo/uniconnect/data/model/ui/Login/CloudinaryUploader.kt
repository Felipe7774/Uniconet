package com.utadeo.uniconnect.data.model.ui.Login

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

object CloudinaryUploader {
    private const val CLOUD_NAME = "dbl3rmiqe"     // <- reemplaza
    private const val UPLOAD_PRESET = "uniconnect_unsigned" // <- reemplaza

    private val client = OkHttpClient()

    /**
     * Sube imagen a Cloudinary usando un unsigned upload preset.
     * imageFile: archivo JPG/PNG local
     * onSuccess(url)
     * onError(message)
     */
    fun uploadImage(imageFile: File, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val mediaType = "image/*".toMediaTypeOrNull()
        val fileBody = imageFile.asRequestBody(mediaType)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", imageFile.name, fileBody)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                onError(e.message ?: "Error en conexión")
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    onError("Respuesta no exitosa: ${response.message}")
                    return
                }
                try {
                    val json = JSONObject(body)
                    val url = json.getString("secure_url")
                    onSuccess(url)
                } catch (t: Throwable) {
                    onError("Parsing response: ${t.message}")
                }
            }
        })
    }
}