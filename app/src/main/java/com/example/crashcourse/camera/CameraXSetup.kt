package com.example.crashcourse.camera

import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Function to set up CameraX with selectable front/back camera
private fun cameraXSetup(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    analyzer: ImageAnalysis.Analyzer,
    executor: ExecutorService,
    useBackCamera: Boolean // Parameter to select the camera
) {
    val context = previewView.context
    ProcessCameraProvider.getInstance(context).apply {
        addListener({
            try {
                val cameraProvider = get()

                // Preview use‑case
                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                // Image‑analysis use‑case
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor, analyzer) }

                cameraProvider.unbindAll()

                // Select camera based on the `useBackCamera` flag
                val cameraSelector = if (useBackCamera) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraXSetup", "Failed to bind use cases", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

// Function to set up CameraX with preview, analysis, and optional image capture
fun cameraXSetupWithCapture(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    analyzer: ImageAnalysis.Analyzer,
    executor: ExecutorService,
    useBackCamera: Boolean,
    imageCapture: ImageCapture? = null // Optional
) {
    val context = previewView.context
    ProcessCameraProvider.getInstance(context).apply {
        addListener({
            try {
                val cameraProvider = get()

                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor, analyzer) }

                cameraProvider.unbindAll()

                val cameraSelector = if (useBackCamera) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }

                val useCases = mutableListOf<UseCase>(preview, imageAnalysis)
                imageCapture?.let { useCases.add(it) }

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    *useCases.toTypedArray()
                )
            } catch (e: Exception) {
                Log.e("CameraXSetup", "Failed to bind use cases", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
}

@Composable
fun CameraPreview(
    analyzer: ImageAnalysis.Analyzer,
    useBackCamera: Boolean // Add useBackCamera parameter to toggle between cameras
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // Keep the same PreviewView instance across recompositions
    val previewView = remember { PreviewView(context) }

    // Single‑thread executor for analysis
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(previewView) {
        cameraXSetup(lifecycleOwner, previewView, analyzer, cameraExecutor, useBackCamera)
        onDispose {
            // No analyzer.close() here—you're responsible for closing each ImageProxy inside analyze()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}
