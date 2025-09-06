package com.example.crashcourse.ui.add

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crashcourse.viewmodel.FaceViewModel
import com.example.crashcourse.utils.PhotoStorageUtils
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

@Composable
fun AddUserScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onUserAdded: () -> Unit
) {
    val viewModel: FaceViewModel = viewModel()
    // UI state
    var name by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var embedding by remember { mutableStateOf<FloatArray?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }
    
    // New capture states
    var showFaceCapture by remember { mutableStateOf(false) }
    var captureMode by remember { mutableStateOf(CaptureMode.EMBEDDING) }

    // Scaffold and snackbar host
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // When isSaved flips to true, show a snackbar
    LaunchedEffect(isSaved) {
        if (isSaved) {
            snackbarHostState.showSnackbar(
                message = "Registered \"$name\" successfully!",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Top
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Add New User", 
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                Spacer(Modifier.height(24.dp))

                // 1. Name and Student ID at the top
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isSaved = false
                    },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = studentId,
                    onValueChange = {
                        studentId = it
                        isSaved = false
                    },
                    label = { Text("Student ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))

                // 2. Card for face/photo actions and status
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Face & Photo", style = MaterialTheme.typography.titleMedium)
                        
                        // Embedding button and status
                        Button(
                            onClick = { 
                                captureMode = CaptureMode.EMBEDDING
                                showFaceCapture = true 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (embedding == null) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(if (embedding == null) "Scan Face for Embedding" 
                                else "Embedding Captured! \u2705")
                        }
                        if (embedding != null) {
                            Text(
                                text = "\u2705 Face embedding captured",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Photo button and preview
                        Button(
                            onClick = { 
                                captureMode = CaptureMode.PHOTO
                                showFaceCapture = true 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (capturedBitmap == null) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(if (capturedBitmap == null) "Capture Photo" 
                                else "Photo Captured! \u2705")
                        }
                        if (capturedBitmap != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    bitmap = capturedBitmap!!.asImageBitmap(),
                                    contentDescription = "User photo",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(end = 12.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                )
                                Text(
                                    text = "\u2705 Photo captured",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // 3. Register button at the bottom
                val context = LocalContext.current
                Button(
                    onClick = {
                        isSubmitting = true
                        if (embedding != null && capturedBitmap != null && name.isNotBlank() && studentId.isNotBlank()) {
                            val photoUrl = try {
                                PhotoStorageUtils.saveFacePhoto(context, capturedBitmap!!, studentId.trim())
                            } catch (e: Exception) {
                                null
                            }
                            if (photoUrl != null) {
                                viewModel.registerFace(
                                    studentId = studentId.trim(),
                                    name = name.trim(),
                                    embedding = embedding!!,
                                    photoUrl = photoUrl,
                                    onSuccess = {
                                        isSaved = true
                                        isSubmitting = false
                                        embedding = null
                                        capturedBitmap = null
                                        name = ""
                                        studentId = ""
                                        onUserAdded()
                                    },
                                    onDuplicate = { existingName ->
                                        isSubmitting = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Duplicate detected: $existingName",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                )
                            } else {
                                isSubmitting = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Failed to save photo",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        } else {
                            isSubmitting = false
                        }
                    },
                    enabled = embedding != null && capturedBitmap != null && 
                              name.isNotBlank() && studentId.isNotBlank() && 
                              !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Register User")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 4. Confirmation text
                if (isSaved) {
                    Text(
                        text = "Registered \"$name\" successfully!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Face capture overlay
            if (showFaceCapture) {
                FaceCaptureScreen(
                    mode = captureMode,
                    onClose = { showFaceCapture = false },
                    onEmbeddingCaptured = { embeddingArray ->
                        embedding = embeddingArray
                    },
                    onPhotoCaptured = { bitmap ->
                        capturedBitmap = bitmap
                    }
                )
            }
        }
    }
}

// Add this to the same file or in a separate file
enum class CaptureMode { EMBEDDING, PHOTO }