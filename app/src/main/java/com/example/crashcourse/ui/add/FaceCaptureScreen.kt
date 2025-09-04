package com.example.crashcourse.ui.add

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.crashcourse.R
import com.example.crashcourse.ml.FaceAnalyzer2
import com.example.crashcourse.ui.FaceOverlay
import com.example.crashcourse.ui.PermissionsHandler
import com.example.crashcourse.utils.toBitmap
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import androidx.camera.view.PreviewView

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceCaptureScreen(
    mode: CaptureMode,
    onClose: () -> Unit,
    onEmbeddingCaptured: (FloatArray) -> Unit = {},
    onPhotoCaptured: (Bitmap) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val executor = remember { Executors.newSingleThreadExecutor() }
    val coroutineScope = rememberCoroutineScope()
    
    // For face detection
    var faceBounds by remember { mutableStateOf(emptyList<Rect>()) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var imageRotation by remember { mutableStateOf(0) }
    var lastEmbedding by remember { mutableStateOf<FloatArray?>(null) }
    
    // UI states
    var isProcessing by remember { mutableStateOf(false) }
    var showCaptureFeedback by remember { mutableStateOf(false) }
    var captureSuccess by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Animation states
    val flashAlpha = remember { Animatable(0f) }
    val checkmarkScale = remember { Animatable(0.5f) }
    
    // For photo capture
    val imageCapture = remember { ImageCapture.Builder().build() }
    
    var useFrontCamera by remember { mutableStateOf(true) }

    val analyzer = remember {
        FaceAnalyzer2 { rect, embedding ->
            android.util.Log.d("FaceOverlayDebug", "Detected rect: $rect, imageSize: $imageSize")
            faceBounds = listOf(rect)
            lastEmbedding = embedding
            imageSize = IntSize(480, 640) // WORKAROUND: set to real camera frame size
            imageRotation = 270 // WORKAROUND: set to real rotation
        }
    }

    // Cleanup resources when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            analyzer.close()
        }
    }

    // Handle capture feedback animation
    LaunchedEffect(showCaptureFeedback) {
        if (showCaptureFeedback) {
            // Flash effect
            flashAlpha.animateTo(1f, animationSpec = tween(100))
            delay(50)
            flashAlpha.animateTo(0f, animationSpec = tween(300))
            
            // Checkmark animation
            checkmarkScale.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            checkmarkScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(300)
            )
            
            // Hide feedback after delay
            delay(1000)
            showCaptureFeedback = false
            capturedBitmap = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PermissionsHandler(permissionState = cameraPermissionState) {
            val previewView = remember { PreviewView(context) }

            // Permission check: use PermissionStatus.Granted
            val hasCameraPermission = cameraPermissionState.status is com.google.accompanist.permissions.PermissionStatus.Granted

            val cameraSelector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

            LaunchedEffect(hasCameraPermission, useFrontCamera) {
                if (hasCameraPermission) {
                    try {
                        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build().also {
                                it.setAnalyzer(executor, analyzer)
                            }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            analysis,
                            imageCapture
                        )

                        // Track analyzer states
                        imageSize = analyzer.imageSize
                        imageRotation = analyzer.imageRotation
                    } catch (e: Exception) {
                        Log.e("FaceCaptureScreen", "Camera setup failed", e)
                    }
                }
            }

            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // FaceOverlay immediately after camera preview, with fillMaxSize
        FaceOverlay(
            faceBounds = faceBounds,
            imageSize = imageSize,
            imageRotation = imageRotation,
            isFrontCamera = useFrontCamera,
            modifier = Modifier.fillMaxSize(), // <-- ensure overlay fills the Box
            paddingFactor = 0.1f
        )
        
        // Flash effect for capture feedback
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = flashAlpha.value))
        )
        
        // Capture feedback UI
        if (showCaptureFeedback) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (captureSuccess) {
                        if (mode == CaptureMode.PHOTO && capturedBitmap != null) {
                            // Show captured photo preview
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = "Captured photo",
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        }
                        
                        // Success checkmark
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.Green.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(50.dp)
                                    .scale(checkmarkScale.value)
                            )
                        }
                        
                        Text(
                            text = when(mode) {
                                CaptureMode.EMBEDDING -> "Embedding Captured!"
                                CaptureMode.PHOTO -> "Photo Captured!"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    } else {
                        // Loading indicator for processing
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            color = Color.White,
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Processing...",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
        }
        
        // Instructions with face detection status
        Text(
            text = if (faceBounds.isEmpty()) {
                "🔍 Searching for face..."
            } else {
                when(mode) {
                    CaptureMode.EMBEDDING -> "✅ Face detected! Tap to capture embedding"
                    CaptureMode.PHOTO -> "✅ Face detected! Tap to take photo"
                }
            },
            color = if (faceBounds.isEmpty()) Color.Yellow else Color.Green,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Action buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Button(
                onClick = onClose,
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    if (isProcessing) return@Button
                    coroutineScope.launch {
                        isProcessing = true
                        showCaptureFeedback = true
                        captureSuccess = false

                        when(mode) {
                            CaptureMode.EMBEDDING -> {
                                lastEmbedding?.let { embedding ->
                                    delay(800) // Artificial delay for better UX
                                    onEmbeddingCaptured(embedding)
                                    captureSuccess = true
                                    delay(1200) // Show success message
                                    isProcessing = false
                                    onClose()
                                } ?: run {
                                    // No embedding available
                                    isProcessing = false
                                    showCaptureFeedback = false
                                }
                            }
                            CaptureMode.PHOTO -> {
                                imageCapture.takePicture(
                                    executor,
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            coroutineScope.launch {
                                                try {
                                                    val bitmap = image.toBitmap()
                                                    capturedBitmap = bitmap
                                                    captureSuccess = true
                                                    delay(1200) // Show success message
                                                    onPhotoCaptured(bitmap)
                                                    isProcessing = false
                                                    onClose()
                                                } catch (e: Exception) {
                                                    Log.e("FaceCaptureScreen", "Bitmap conversion failed", e)
                                                    captureSuccess = false
                                                    isProcessing = false
                                                    showCaptureFeedback = false
                                                } finally {
                                                    image.close()
                                                }
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            Log.e("FaceCaptureScreen", "Photo capture failed", exception)
                                            coroutineScope.launch {
                                                captureSuccess = false
                                                isProcessing = false
                                                showCaptureFeedback = false
                                                onClose()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                enabled = !isProcessing && when(mode) {
                    CaptureMode.EMBEDDING -> lastEmbedding != null
                    CaptureMode.PHOTO -> true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(when(mode) {
                        CaptureMode.EMBEDDING -> "Capture Embedding"
                        CaptureMode.PHOTO -> "Take Photo"
                    })
                }
            }
        }
        
        // Close button at top right
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            enabled = !isProcessing
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close camera",
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            )
        }
        
        // Camera switch button (top left)
        IconButton(
            onClick = { useFrontCamera = !useFrontCamera },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = if (useFrontCamera) "Switch to back camera" else "Switch to front camera",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Face alignment guide
        if (!showCaptureFeedback && faceBounds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.face_outline),
                    contentDescription = "Face alignment guide",
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
}