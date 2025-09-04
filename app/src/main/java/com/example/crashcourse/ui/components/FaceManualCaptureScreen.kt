package com.example.crashcourse.ui

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.crashcourse.camera.ManualFaceDetectionAnalyzer
import com.example.crashcourse.camera.ManualFaceBoxOverlay
import com.example.crashcourse.camera.toBitmap
import java.util.concurrent.Executors

/**
 * FaceManualCaptureScreen - Independent manual face capture screen
 * This screen is completely separate from the main attendance system
 * Features:
 * - Green face detection boxes
 * - Manual capture button (only enabled when face detected)
 * - Independent camera setup
 */
@Composable
fun FaceManualCaptureScreen(
    onCaptured: (Bitmap) -> Unit,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val overlay = remember { ManualFaceBoxOverlay(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var faceDetected by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }

    // Cleanup executor on dispose
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Camera preview with face detection overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AndroidView(
                factory = {
                    FrameLayout(it).apply {
                        addView(previewView)
                        addView(overlay)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Face detection status indicator
            if (faceDetected) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "✅ Face Detected - Ready to Capture!",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Capture button
            Button(
                onClick = {
                    if (!isCapturing && faceDetected) {
                        isCapturing = true
                        imageCapture.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    try {
                                        val bitmap = image.toBitmap()
                                        onCaptured(bitmap)
                                    } catch (e: Exception) {
                                        Log.e("FaceManualCapture", "Failed to convert image", e)
                                    } finally {
                                        image.close()
                                        isCapturing = false
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("FaceManualCapture", "Capture failed", exception)
                                    isCapturing = false
                                }
                            }
                        )
                    }
                },
                enabled = faceDetected && !isCapturing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (faceDetected) "📷 Capture Face" else "👤 Position Face in View",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cancel button
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    }

    // Initialize camera
    LaunchedEffect(true) {
        try {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(
                cameraExecutor,
                ManualFaceDetectionAnalyzer { faces ->
                    // Update overlay with face bounds
                    overlay.faceBounds = faces.map { it.boundingBox }
                    faceDetected = faces.isNotEmpty()
                }
            )

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                analyzer
            )
        } catch (e: Exception) {
            Log.e("FaceManualCapture", "Failed to initialize camera", e)
        }
    }
}
