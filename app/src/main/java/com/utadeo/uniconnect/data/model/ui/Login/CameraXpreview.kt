package com.utadeo.uniconnect.data.model.ui.Login

import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Vista composable que muestra la cámara frontal en tiempo real,
 * analiza los frames con FaceLivenessProcessor y devuelve el bitmap al verificar.
 */
@Composable
fun CameraXPreview(
    modifier: Modifier = Modifier,
    onVerified: (Bitmap) -> Unit,
    onStatus: (String) -> Unit
) {
    val ctx = LocalContext.current
    val lifecycleOwner = ctx as? LifecycleOwner
        ?: throw IllegalStateException("Context is not a LifecycleOwner (usa una ComponentActivity).")

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()

                    val faceProcessor = FaceLivenessProcessor(
                        onVerified = { bitmap ->
                            onStatus("Verificado")
                            onVerified(bitmap)
                        },
                        onStatus = { status -> onStatus(status) }
                    )

                    imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                        faceProcessor.processImageProxy(imageProxy)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    Log.e("CameraXPreview", "Error binding camera use cases: ${e.message}", e)
                    onStatus("Error al iniciar cámara: ${e.message ?: "unknown"}")
                }
            }, ContextCompat.getMainExecutor(context))

            previewView
        },
        modifier = modifier
    )
}
