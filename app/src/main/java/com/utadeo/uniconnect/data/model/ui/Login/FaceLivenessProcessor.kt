package com.utadeo.uniconnect.data.model.ui.Login

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * Procesa las imágenes de la cámara para verificar el rostro (liveness).
 * Cuando detecta un rostro válido, devuelve el bitmap listo para subir a Cloudinary.
 */
class FaceLivenessProcessor(
    private val onVerified: (Bitmap) -> Unit,
    private val onStatus: (String) -> Unit
) {
    private var verified = false

    fun processImageProxy(image: ImageProxy) {
        try {
            val bitmap = image.toBitmap() // Convierte el frame a Bitmap

            if (!verified && detectFace(bitmap)) {
                verified = true
                onVerified(bitmap)
            } else if (!verified) {
                onStatus("Detectando rostro...")
            }

        } catch (e: Exception) {
            Log.e("FaceLiveness", "Error procesando imagen: ${e.message}")
            onStatus("Error procesando imagen")
        } finally {
            image.close()
        }
    }

    /**
     * Simula la detección facial.
     * Aquí podrías integrar ML Kit o Face Detection real si lo deseas.
     */
    private fun detectFace(bitmap: Bitmap): Boolean {
        return true // por ahora, simula detección exitosa
    }
}

/**
 * Convierte el ImageProxy (YUV_420_888) a un Bitmap RGB visible.
 */
fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // Convertimos YUV_420_888 → NV21
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)
    val imageBytes = out.toByteArray()
    var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    // Si es cámara frontal, reflejamos la imagen (selfie)
    val matrix = Matrix().apply { preScale(-1f, 1f) }
    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    return bitmap
}
