package com.example.crashcourse.camera

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.Executors

@Composable
fun CameraCaptureScreen(
    useBackCamera: Boolean = false,
    onPhotoCaptured: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }

    DisposableEffect(previewView) {
        cameraXSetupWithCapture(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            analyzer = ImageAnalysis.Analyzer { /* no-op for photo capture */ },
            executor = cameraExecutor,
            useBackCamera = useBackCamera,
            imageCapture = imageCapture
        )
        onDispose { cameraExecutor.shutdown() }
    }

    var isCapturing by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = {
                isCapturing = true
                imageCapture.takePicture(
                    cameraExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                            val bitmap = image.toBitmap() // Your extension function
                            onPhotoCaptured(bitmap)
                            image.close()
                            isCapturing = false
                        }
                        override fun onError(exception: ImageCaptureException) {
                            isCapturing = false
                        }
                    }
                )
            },
            enabled = !isCapturing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Capture Photo")
        }
    }
}
