package com.example.crashcourse.ui

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crashcourse.ui.components.FaceViewModel
import com.example.crashcourse.utils.RealPhotoCaptureManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

/**
 * 📷 RealPhotoCaptureScreen - UI for real photo capture
 * 
 * Features:
 * - ✅ Camera preview with CameraX
 * - ✅ Real photo capture button
 * - ✅ Progress indicators
 * - ✅ Error handling
 * - ✅ Permission management
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RealPhotoCaptureScreen(
    studentId: String,
    name: String,
    className: String = "",
    subClass: String = "",
    grade: String = "",
    subGrade: String = "",
    program: String = "",
    role: String = "",
    useBackCamera: Boolean = true,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: FaceViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var isCapturing by remember { mutableStateOf(false) }
    var progressMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Camera selection state - this will trigger recomposition
    var currentCameraIsBack by remember { mutableStateOf(useBackCamera) }

    // Initialize photo capture manager
    val photoCaptureManager = remember {
        RealPhotoCaptureManager(context, lifecycleOwner)
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            photoCaptureManager.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (cameraPermissionState.status.isGranted) {
            // Camera preview
            val previewView = remember { PreviewView(context) }

            // Initialize camera
            LaunchedEffect(currentCameraIsBack) {
                val success = photoCaptureManager.initializeCamera(previewView, currentCameraIsBack)
                if (!success) {
                    onError("Failed to initialize camera")
                }
            }

            // Camera preview
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // UI Overlay
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top bar with close and camera switch buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    // Camera switch button
                    IconButton(
                        onClick = {
                            if (!isCapturing) {
                                currentCameraIsBack = !currentCameraIsBack
                            }
                        },
                        enabled = !isCapturing,
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }

                    // Info card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = name,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "ID: $studentId",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = if (currentCameraIsBack) "📷 Back Camera" else "🤳 Front Camera",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
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
                    // Progress indicator
                    if (isCapturing) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = progressMessage,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Error message
                    if (errorMessage.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    // Capture button
                    FloatingActionButton(
                        onClick = {
                            if (!isCapturing) {
                                scope.launch {
                                    isCapturing = true
                                    errorMessage = ""

                                    photoCaptureManager.captureAndProcessPhoto(
                                        studentId = studentId,
                                        name = name,
                                        viewModel = viewModel,
                                        className = className,
                                        subClass = subClass,
                                        grade = grade,
                                        subGrade = subGrade,
                                        program = program,
                                        role = role,
                                        onSuccess = {
                                            isCapturing = false
                                            onSuccess()
                                        },
                                        onError = { error ->
                                            isCapturing = false
                                            errorMessage = error
                                            onError(error)
                                        },
                                        onProgress = { message ->
                                            progressMessage = message
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.size(72.dp),
                        containerColor = if (isCapturing) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Capture Photo",
                            modifier = Modifier.size(32.dp),
                            tint = if (isCapturing) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instructions
                    Text(
                        text = if (isCapturing) {
                            "Processing..."
                        } else {
                            "Position your face in the center and tap to capture"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

        } else {
            // Permission request UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera permission is required to capture photos",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() }
                ) {
                    Text("Grant Camera Permission")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.White)
                }
            }
        }
    }
}
