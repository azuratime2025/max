package com.example.crashcourse.ui

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crashcourse.scanner.FaceScanner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import java.util.*
import com.example.crashcourse.utils.cosineDistance
import com.example.crashcourse.db.FaceCache
import com.example.crashcourse.viewmodel.FaceViewModel
import com.example.crashcourse.ml.FaceRecognizer


@Composable
fun CheckInScreen(
    useBackCamera: Boolean
) {
    val context = LocalContext.current

    // Camera switching state
    var currentCameraIsBack by remember { mutableStateOf(useBackCamera) }

    var gallery by remember { mutableStateOf<List<Pair<String, FloatArray>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var matchName by remember { mutableStateOf<String?>(null) }
    var isRegistered by remember { mutableStateOf(true) }
    var alreadyCheckedIn by remember { mutableStateOf(false) }
    var greeting by remember { mutableStateOf("") }

    val checkInTimestamps = remember { mutableStateMapOf<String, Long>() }


    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    // Initialize TTS
    LaunchedEffect(Unit) {
        TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.US
                tts.value?.setSpeechRate(1.0f)
            }
        }.also {
            tts.value = it
        }

        // Set device media volume to max
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
    }

    fun speak(message: String) {
        tts.value?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Dispose TTS properly
    DisposableEffect(Unit) {
        onDispose {
            tts.value?.shutdown()
        }
    }

    // Load face gallery
    LaunchedEffect(Unit) {
        gallery = FaceCache.load(context)
        loading = false
    }



    Box(Modifier.fillMaxSize()) {
        if (loading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))

        } else {
            FaceScanner(useBackCamera = currentCameraIsBack) { _, embedding ->
                val best = gallery.minByOrNull { cosineDistance(it.second, embedding) }
                if (best != null && cosineDistance(best.second, embedding) < 0.4f) {
                    val name = best.first
                    val now = System.currentTimeMillis()
                    val lastCheckIn = checkInTimestamps[name] ?: 0L

                    if (now - lastCheckIn > 60_000) {
                        checkInTimestamps[name] = now
                        matchName = name
                        isRegistered = true
                        alreadyCheckedIn = false

                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        greeting = when (hour) {
                            in 5..11 -> "Good morning"
                            in 12..17 -> "Good afternoon"
                            else -> "Good evening"
                        }

                        speak("Thanks")
                    } else {
                        matchName = name
                        isRegistered = true
                        alreadyCheckedIn = true
                        // No voice for already checked in
                    }
                } else {
                    matchName = null
                    isRegistered = false
                    alreadyCheckedIn = false
                    // No voice for not registered
                }
            }

            // YouTube-style header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App title
                Text(
                    text = "Azura",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Camera indicator and switch button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Camera indicator
                    Text(
                        text = if (currentCameraIsBack) "Back" else "Front",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                CircleShape
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )

                    // Camera switch button
                    IconButton(
                        onClick = {
                            currentCameraIsBack = !currentCameraIsBack
                        },
                        modifier = Modifier
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }
                }
            }

            matchName?.let { name ->
                if (alreadyCheckedIn) {
                    Text(
                        text = "$name Already Checkin",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 80.dp)
                            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color(0xFF008080), // Teal color
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    Text(
                        text = "$name Checkin at $hour:00",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 80.dp)
                            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color(0xFF008080), // Teal color
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            if (!isRegistered) {
                Text(
                    text = "Not Registered",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 32.dp)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            CircleShape
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error)
                )
            }
        }
    }
}
