package com.example.crashcourse.ui

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.crashcourse.scanner.FaceScanner
import com.example.crashcourse.utils.PhotoProcessingUtils
import com.example.crashcourse.utils.PhotoStorageUtils
import com.example.crashcourse.utils.showToast
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoCaptureScreen(
    useBackCamera: Boolean,
    onPhotoCaptured: (Bitmap, FloatArray) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var isProcessing by remember { mutableStateOf(false) }
    var captureMode by remember { mutableStateOf(CaptureMode.CAMERA) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            scope.launch {
                isProcessing = true
                try {
                    val bitmap = PhotoStorageUtils.loadBitmapFromUri(context, selectedUri)
                    if (bitmap != null) {
                        val result = PhotoProcessingUtils.processBitmapForFaceEmbedding(context, bitmap)
                        if (result != null) {
                            val (faceBitmap, embedding) = result
                            onPhotoCaptured(faceBitmap, embedding)
                            context.showToast("Photo processed successfully!")
                        } else {
                            context.showToast("No face detected in the selected image")
                        }
                    } else {
                        context.showToast("Failed to load selected image")
                    }
                } catch (e: Exception) {
                    context.showToast("Error processing image: ${e.message}")
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (captureMode) {
            CaptureMode.CAMERA -> {
                if (cameraPermissionState.status.isGranted) {
                    // Camera view
                    FaceScanner(
                        useBackCamera = useBackCamera,
                        onFaceEmbedding = { _: Rect, embedding: FloatArray ->
                            scope.launch {
                                isProcessing = true
                                try {
                                    // Create a bitmap from the detected face
                                    // For now, we'll create a placeholder and use the embedding
                                    val faceBitmap = createBitmap(160, 160)
                                    onPhotoCaptured(faceBitmap, embedding)
                                    context.showToast("Face captured successfully!")
                                } catch (e: Exception) {
                                    context.showToast("Error capturing face: ${e.message}")
                                } finally {
                                    isProcessing = false
                                }
                            }
                        }
                    )
                } else {
                    // Permission request UI
                    PermissionRequestUI(
                        onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                        onDismiss = onDismiss
                    )
                }
            }
            CaptureMode.GALLERY -> {
                // Gallery selection initiated
                LaunchedEffect(Unit) {
                    galleryLauncher.launch("image/*")
                    captureMode = CaptureMode.CAMERA
                }
            }
        }

        // Top bar with controls
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Capture Face Photo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Gallery button
                    IconButton(
                        onClick = {
                            captureMode = CaptureMode.GALLERY
                        },
                        enabled = !isProcessing
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Select from gallery"
                        )
                    }

                    // Close button
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
        }

        // Bottom instructions
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = "Processing photo...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Position your face in the camera view",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Face will be captured automatically when detected",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { captureMode = CaptureMode.GALLERY },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gallery")
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestUI(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "To capture photos, please grant camera permission.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onRequestPermission,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}

private enum class CaptureMode {
    CAMERA,
    GALLERY
}
