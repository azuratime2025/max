package com.example.crashcourse.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

/**
 * Utility class for processing photos from various sources during bulk registration
 */
object BulkPhotoProcessor {
    private const val TAG = "BulkPhotoProcessor"
    
    // Photo processing constants
    private const val MAX_PHOTO_SIZE = 512 // pixels
    private const val JPEG_QUALITY = 80 // compression quality for bulk processing
    private const val MAX_FILE_SIZE = 500 * 1024 // 500KB max per photo
    private const val DOWNLOAD_TIMEOUT = 30L // seconds
    
    // HTTP client for downloading photos
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(DOWNLOAD_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(DOWNLOAD_TIMEOUT, TimeUnit.SECONDS)
        .build()
    
    /**
     * Photo processing result
     */
    data class PhotoProcessResult(
        val success: Boolean,
        val localPhotoUrl: String? = null,
        val error: String? = null,
        val originalSize: Long = 0,
        val processedSize: Long = 0
    )
    
    /**
     * Process photo from various sources (URL, local file, base64)
     */
    suspend fun processPhotoSource(
        context: Context,
        photoSource: String,
        studentId: String
    ): PhotoProcessResult {
        if (photoSource.isBlank()) {
            return PhotoProcessResult(
                success = true,
                localPhotoUrl = null,
                error = null
            )
        }
        
        return try {
            Log.d(TAG, "Processing photo for $studentId: ${photoSource.take(100)}...")
            
            val bitmap = when {
                photoSource.startsWith("http://") || photoSource.startsWith("https://") -> {
                    downloadPhotoFromUrl(photoSource)
                }
                photoSource.startsWith("data:image") -> {
                    decodeBase64Photo(photoSource)
                }
                photoSource.startsWith("file://") -> {
                    loadLocalPhoto(photoSource.removePrefix("file://"))
                }
                else -> {
                    // Try as local file path
                    loadLocalPhoto(photoSource)
                }
            }
            
            if (bitmap == null) {
                return PhotoProcessResult(
                    success = false,
                    error = "Failed to load image from source"
                )
            }
            
            // Optimize the bitmap
            val optimizedBitmap = optimizePhoto(bitmap)
            
            // Save using existing PhotoStorageUtils
            val savedPhotoUrl = PhotoStorageUtils.saveFacePhoto(context, optimizedBitmap, studentId)
            
            if (savedPhotoUrl != null) {
                val savedFile = File(savedPhotoUrl)
                Log.d(TAG, "Photo processed successfully for $studentId: ${savedFile.length()} bytes")
                
                PhotoProcessResult(
                    success = true,
                    localPhotoUrl = savedPhotoUrl,
                    processedSize = savedFile.length()
                )
            } else {
                PhotoProcessResult(
                    success = false,
                    error = "Failed to save processed photo"
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing photo for $studentId", e)
            PhotoProcessResult(
                success = false,
                error = "Processing failed: ${e.message}"
            )
        }
    }
    
    /**
     * Download photo from HTTP/HTTPS URL
     */
    private suspend fun downloadPhotoFromUrl(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Downloading photo from: $url")
                
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "AzuraApp/1.0")
                    .build()
                
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Download failed with code: ${response.code}")
                        return@withContext null
                    }
                    
                    val contentLength = response.body?.contentLength() ?: 0
                    if (contentLength > MAX_FILE_SIZE * 2) { // Allow larger downloads, we'll compress
                        Log.w(TAG, "Downloaded file is large: $contentLength bytes")
                    }
                    
                    response.body?.byteStream()?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download photo from $url", e)
                null
            }
        }
    }
    
    /**
     * Decode base64 encoded photo
     */
    private fun decodeBase64Photo(dataUrl: String): Bitmap? {
        return try {
            Log.d(TAG, "Decoding base64 photo...")
            
            // Extract base64 data from data URL
            val base64Data = if (dataUrl.contains(",")) {
                dataUrl.substringAfter(",")
            } else {
                dataUrl
            }
            
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode base64 photo", e)
            null
        }
    }
    
    /**
     * Load photo from local file path
     */
    private fun loadLocalPhoto(filePath: String): Bitmap? {
        return try {
            Log.d(TAG, "Loading local photo: $filePath")
            
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "Local photo file does not exist: $filePath")
                return null
            }
            
            if (file.length() > MAX_FILE_SIZE * 2) {
                Log.w(TAG, "Local photo file is large: ${file.length()} bytes")
            }
            
            FileInputStream(file).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load local photo: $filePath", e)
            null
        }
    }
    
    /**
     * Optimize photo for face recognition and storage
     */
    private fun optimizePhoto(bitmap: Bitmap): Bitmap {
        try {
            // Calculate optimal size
            val maxDimension = MAX_PHOTO_SIZE
            val width = bitmap.width
            val height = bitmap.height
            
            if (width <= maxDimension && height <= maxDimension) {
                // Already optimal size
                return bitmap
            }
            
            // Calculate new dimensions maintaining aspect ratio
            val ratio = minOf(
                maxDimension.toFloat() / width,
                maxDimension.toFloat() / height
            )
            
            val newWidth = (width * ratio).toInt()
            val newHeight = (height * ratio).toInt()
            
            Log.d(TAG, "Resizing photo from ${width}x${height} to ${newWidth}x${newHeight}")
            
            return bitmap.scale(newWidth, newHeight)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to optimize photo, using original", e)
            return bitmap
        }
    }
    
    /**
     * Validate photo quality for face recognition
     */
    fun validatePhotoQuality(bitmap: Bitmap): List<String> {
        val issues = mutableListOf<String>()
        
        // Size validation
        if (bitmap.width < 160 || bitmap.height < 160) {
            issues.add("Photo too small (minimum 160x160 pixels)")
        }
        
        if (bitmap.width > 2048 || bitmap.height > 2048) {
            issues.add("Photo very large (will be resized)")
        }
        
        // Aspect ratio validation
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        if (aspectRatio < 0.5f || aspectRatio > 2.0f) {
            issues.add("Unusual aspect ratio (may affect face detection)")
        }
        
        return issues
    }
    
    /**
     * Get photo source type for logging/debugging
     */
    fun getPhotoSourceType(photoSource: String): String {
        return when {
            photoSource.isBlank() -> "None"
            photoSource.startsWith("http://") || photoSource.startsWith("https://") -> "HTTP URL"
            photoSource.startsWith("data:image") -> "Base64 Data"
            photoSource.startsWith("file://") -> "File URL"
            else -> "Local Path"
        }
    }
    
    /**
     * Estimate processing time based on photo source
     */
    fun estimateProcessingTime(photoSources: List<String>): Long {
        var totalSeconds = 0L
        
        photoSources.forEach { source ->
            totalSeconds += when {
                source.isBlank() -> 0
                source.startsWith("http") -> 3 // Download + process
                source.startsWith("data:") -> 1 // Decode + process
                else -> 1 // Load + process
            }
        }
        
        return totalSeconds
    }
}
