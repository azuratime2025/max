package com.example.crashcourse.scanner

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.example.crashcourse.ml.FaceAnalyzer

import com.example.crashcourse.ui.FaceOverlay
import com.example.crashcourse.ui.PermissionsHandler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors
import com.google.accompanist.permissions.isGranted


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceScanner(
    useBackCamera: Boolean = false, // ✅ Accept useBackCamera from caller
    onFaceEmbedding: (Rect, FloatArray) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    PermissionsHandler(permissionState = cameraPermissionState) {
        val executor by remember { mutableStateOf(Executors.newSingleThreadExecutor()) }
        DisposableEffect(Unit) {
            onDispose { executor.shutdown() }
        }

        val analyzer = remember(onFaceEmbedding) {
            FaceAnalyzer(onFaceEmbedding)
        }

        val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }

        LaunchedEffect(cameraPermissionState.status, useBackCamera) {
            if (cameraPermissionState.status.isGranted) {
                val cameraProvider = ProcessCameraProvider.getInstance(context).get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().also {
                        it.setAnalyzer(executor, analyzer)
                    }

                val selector = if (useBackCamera) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
            }
        }

        Box(Modifier.fillMaxSize()) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            FaceOverlay(
                faceBounds = analyzer.faceBounds,
                imageSize = analyzer.imageSize,
                isFrontCamera = !useBackCamera, // ✅ Flip based on camera being used
                paddingFactor = 0.1f,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


