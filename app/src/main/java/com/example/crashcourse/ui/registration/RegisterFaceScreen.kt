package com.example.crashcourse.ui

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crashcourse.scanner.FaceScanner
import com.example.crashcourse.utils.FaceImageSaver
import com.example.crashcourse.utils.PhotoStorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import androidx.core.graphics.createBitmap

import com.example.crashcourse.db.ClassOption
import com.example.crashcourse.db.SubClassOption
import com.example.crashcourse.db.GradeOption
import com.example.crashcourse.db.SubGradeOption
import com.example.crashcourse.db.ProgramOption
import com.example.crashcourse.db.RoleOption
import kotlinx.coroutines.launch
import java.util.*
import com.example.crashcourse.viewmodel.FaceViewModel

@Composable
fun RegisterFaceScreen(
    useBackCamera: Boolean,
    vm: FaceViewModel
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }

    // Add these string variables for the dropdown values
    var className by remember { mutableStateOf("") }
    var subClass by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var subGrade by remember { mutableStateOf("") }
    var program by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }



    // For dropdown selections
    var selectedClassId by remember { mutableStateOf<Int?>(null) }
    @Suppress("UNUSED_VARIABLE")
    var selectedProgramId by remember { mutableStateOf<Int?>(null) }
    @Suppress("UNUSED_VARIABLE")
    var selectedRoleId by remember { mutableStateOf<Int?>(null) }

    // For dropdown expanded states
    @Suppress("UNUSED_VARIABLE")
    var subGradeExpanded by remember { mutableStateOf(false) }
    @Suppress("UNUSED_VARIABLE")
    var programExpanded by remember { mutableStateOf(false) }
    @Suppress("UNUSED_VARIABLE")
    var roleExpanded by remember { mutableStateOf(false) }

    // Get dropdown options from ViewModel
    @Suppress("UNUSED_VARIABLE")
    val programOptions by vm.programOptions.collectAsState()
    @Suppress("UNUSED_VARIABLE")
    val roleOptions by vm.roleOptions.collectAsState()

    // For dependent dropdowns
    @Suppress("UNUSED_VARIABLE")
    val subGradeOptions: List<SubGradeOption> = if (selectedClassId != null) {
        vm.getSubGradeOptions(selectedClassId!!).collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }

    var embedding by remember { mutableStateOf<FloatArray?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }
    var faceDetected by remember { mutableStateOf(false) }

    // Camera selection state
    var selectedCameraIsBack by remember { mutableStateOf(useBackCamera) }

    // Populate initial options if needed
    LaunchedEffect(Unit) {
        vm.populateInitialOptions()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // TTS
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(Unit) {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.US
                tts.value?.setSpeechRate(1.0f)
            }
        }.also {
            tts.value = it
        }

        // Set volume to max
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
    }

    fun speak(message: String) {
        tts.value?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.value?.shutdown()
        }
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            speak("Welcome $name")
            snackbarHostState.showSnackbar(
                message = "Registered \"$name\" successfully!",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Clear face detection status after a delay
    LaunchedEffect(faceDetected) {
        if (faceDetected) {
            kotlinx.coroutines.delay(3000) // Wait 3 seconds
            faceDetected = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Full-screen camera
            Box(modifier = Modifier.fillMaxSize()) {
                FaceScanner(useBackCamera = selectedCameraIsBack) { _, newEmbedding ->
                    embedding = newEmbedding
                    faceDetected = true
                    isSaved = false
                    // Note: Bitmap capture is not directly available from FaceScanner in this implementation
                    // Will use a placeholder or alternative method for image capture in future updates
                    capturedBitmap = null
                }

                // Camera switch icon on top right
                IconButton(
                    onClick = { selectedCameraIsBack = !selectedCameraIsBack },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Face detection status overlay
                if (faceDetected && embedding != null) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Face detected",
                                tint = Color(0xFF008080), // Teal color
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Face Detected!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF008080) // Teal color
                            )
                        }
                    }
                }

                // Input fields and register button at the bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            isSaved = false
                        },
                        label = { Text("Name", color = Color(0xFF008080)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF008080), // Teal color
                            unfocusedBorderColor = Color(0xFF008080).copy(alpha = 0.5f)
                        )
                    )

                    OutlinedTextField(
                        value = studentId,
                        onValueChange = {
                            studentId = it
                            isSaved = false
                        },
                        label = { Text("Student ID", color = Color(0xFF008080)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF008080), // Teal color
                            unfocusedBorderColor = Color(0xFF008080).copy(alpha = 0.5f)
                        )
                    )

                    // Submit button (with captured photo)
                    Button(
                        onClick = {
                            isSubmitting = true
                            embedding?.let { emb ->
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val finalStudentId = studentId.ifEmpty { "STU" + System.currentTimeMillis().toString() }

                                        // Save the captured photo if available, otherwise create and save a placeholder bitmap
                                        var photoUrl: String? = null
                                        try {
                                            photoUrl = capturedBitmap?.let { bitmap ->
                                                Log.d("RegisterFaceScreen", "Saving captured bitmap for student: $finalStudentId")
                                                PhotoStorageUtils.saveFacePhoto(context, bitmap, finalStudentId)
                                            } ?: run {
                                                Log.d("RegisterFaceScreen", "Creating and saving placeholder bitmap for student: $finalStudentId")
                                                val placeholderBitmap = createPlaceholderBitmap(name.trim())
                                                PhotoStorageUtils.saveFacePhoto(context, placeholderBitmap, finalStudentId)
                                            }
                                            Log.d("RegisterFaceScreen", "Photo saved successfully at: $photoUrl")
                                        } catch (saveException: Exception) {
                                            Log.e("RegisterFaceScreen", "Failed to save photo: ${saveException.message}", saveException)
                                            photoUrl = "https://picsum.photos/seed/consistent/200/200" // Fallback to placeholder URL if saving fails
                                        }
                                        
                                        withContext(Dispatchers.Main) {
                                            Log.d("RegisterFaceScreen", "Registering face with studentId: $finalStudentId, name: ${name.trim()}, photoUrl: $photoUrl")
                                            vm.registerFace(
                                                studentId = finalStudentId,
                                                name = name.trim(),
                                                embedding = emb,
                                                photoUrl = photoUrl,
                                                className = className,
                                                subClass = subClass,
                                                grade = grade,
                                                subGrade = subGrade,
                                                program = program,
                                                role = role,
                                                onSuccess = {
                                                    Log.d("RegisterFaceScreen", "Face registered successfully for $name")
                                                    isSaved = true
                                                    isSubmitting = false
                                                    embedding = null
                                                    capturedBitmap = null // Clear the bitmap after saving
                                                },
                                                onDuplicate = { existingName ->
                                                    Log.w("RegisterFaceScreen", "Duplicate face registration detected as $existingName")
                                                    isSubmitting = false
                                                    speak("This face is already registered as $existingName")
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Already registered as \"$existingName\"",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    } catch (e: Exception) {
                                        Log.e("RegisterFaceScreen", "Registration error: ${e.message}", e)
                                        withContext(Dispatchers.Main) {
                                            isSubmitting = false
                                            speak("Registration failed")
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Registration failed: ${e.message}",
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        }
                                    }
                                }
                            } ?: run {
                                speak("Try again")
                                isSubmitting = false
                            }
                        },
                        enabled = name.isNotBlank() && embedding != null && !isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF008080), // Teal color
                            disabledContainerColor = Color(0xFF008080).copy(alpha = 0.5f)
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Register Face", color = Color.White)
                        }
                    }

                    if (isSaved) {
                        Text(
                            text = "Registered \"$name\"!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF008080), // Teal color
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
                // Loading overlay during saving
                if (isSubmitting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp,
                                color = Color(0xFF008080) // Teal color
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Saving Face Data...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Create a placeholder bitmap with the person's name
 */
private fun createPlaceholderBitmap(name: String): android.graphics.Bitmap {
    val bitmap = createBitmap(200, 200)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLUE
        textSize = 20f
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }

    // Draw background
    canvas.drawColor(android.graphics.Color.WHITE)

    // Draw name
    canvas.drawText(name, 100f, 80f, paint)

    // Draw a simple avatar
    paint.color = android.graphics.Color.BLACK
    canvas.drawCircle(100f, 130f, 40f, paint) // Head
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(90f, 120f, 6f, paint) // Left eye
    canvas.drawCircle(110f, 120f, 6f, paint) // Right eye
    canvas.drawCircle(100f, 135f, 3f, paint) // Nose

    // Draw smile
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 2f
    canvas.drawArc(85f, 140f, 115f, 155f, 0f, 180f, false, paint)

    return bitmap
}