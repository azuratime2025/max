package com.example.crashcourse.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File

/**
 * FaceAvatar - Composable function for displaying user face avatars
 * 
 * Loads face images from local storage using Coil with fallback to default icon
 * Displays images in circular 64dp format with proper error handling
 */

private const val TAG = "FaceAvatar"

@Composable
fun FaceAvatar(
    photoPath: String?,
    modifier: Modifier = Modifier,
    size: Int = 64
) {
    val context = LocalContext.current
    
    Log.d(TAG, "FaceAvatar called with photoPath: '$photoPath'")
    Log.d(TAG, "  photoPath is null: ${photoPath == null}")
    Log.d(TAG, "  photoPath is empty: ${photoPath?.isEmpty()}")
    Log.d(TAG, "  photoPath length: ${photoPath?.length}")

    // Check if photo path is valid (either URL or local file)
    val isUrl = !photoPath.isNullOrEmpty() && (photoPath.startsWith("http://") || photoPath.startsWith("https://"))
    val isLocalFile = !photoPath.isNullOrEmpty() && !isUrl && File(photoPath).exists()
    val hasValidPhoto = isUrl || isLocalFile

    Log.d(TAG, "  Is URL: $isUrl")
    Log.d(TAG, "  Is local file: $isLocalFile")
    Log.d(TAG, "  Has valid photo: $hasValidPhoto")

    if (!photoPath.isNullOrEmpty() && !isUrl) {
        val file = File(photoPath)
        Log.d(TAG, "  File absolute path: ${file.absolutePath}")
        Log.d(TAG, "  File exists: ${file.exists()}")
        Log.d(TAG, "  File readable: ${file.canRead()}")
        Log.d(TAG, "  File size: ${if (file.exists()) file.length() else "N/A"} bytes")
    }

    if (hasValidPhoto) {
        Log.d(TAG, "Loading image from: $photoPath")

        // Determine the data source (URL or File)
        val imageData = if (photoPath!!.startsWith("http://") || photoPath.startsWith("https://")) {
            photoPath // Use URL directly
        } else {
            File(photoPath) // Use File for local paths
        }

        // Load image using Coil with proper error handling
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(imageData)
                    .crossfade(true)
                    .listener(
                        onStart = {
                            Log.d(TAG, "🔄 Image loading started: $photoPath")
                        },
                        onSuccess = { _, result ->
                            Log.d(TAG, "✅ Image loaded successfully: $photoPath")
                            Log.d(TAG, "  Result: ${result.drawable}")
                        },
                        onError = { _, error ->
                            Log.e(TAG, "❌ Image loading failed: $photoPath")
                            Log.e(TAG, "  Error: ${error.throwable?.message}")
                            Log.e(TAG, "  Error type: ${error.throwable?.javaClass?.simpleName}")
                            error.throwable?.printStackTrace()
                        }
                    )
                    .build()
            ),
            contentDescription = "User Photo",
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Log.d(TAG, "Using default avatar - photoPath: $photoPath")
        if (!photoPath.isNullOrEmpty()) {
            Log.d(TAG, "  Is URL: ${photoPath.startsWith("http")}")
            Log.d(TAG, "  File exists: ${File(photoPath).exists()}")
        }

        // Show default person icon with same styling
        Card(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Avatar",
                    modifier = Modifier.size((size * 0.6).dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}



/**
 * Small FaceAvatar for list items
 */
@Composable
fun SmallFaceAvatar(
    photoPath: String?,
    modifier: Modifier = Modifier
) {
    FaceAvatar(photoPath = photoPath, modifier = modifier, size = 48)
}

/**
 * Large FaceAvatar for profile screens
 */
@Composable
fun LargeFaceAvatar(
    photoPath: String?,
    modifier: Modifier = Modifier
) {
    FaceAvatar(photoPath = photoPath, modifier = modifier, size = 96)
}

/**
 * Utility function to check if a photo path is valid
 */
fun isValidPhotoPath(photoPath: String?): Boolean {
    return !photoPath.isNullOrEmpty() && File(photoPath).exists().also { exists ->
        Log.d(TAG, "Photo path validation - Path: $photoPath, Exists: $exists")
    }
}
