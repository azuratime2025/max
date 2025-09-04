package com.example.crashcourse.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object PhotoStorageUtils {
    private const val FACE_FOLDER_NAME = "faces"
    private const val TAG = "PhotoStorageUtils"
    
    /**
     * Get the faces directory, creating it if it doesn't exist
     */
    fun getFacesDirectory(context: Context): File {
        val facesDir = File(context.filesDir, FACE_FOLDER_NAME)
        if (!facesDir.exists()) {
            facesDir.mkdirs()
        }
        return facesDir
    }
    
    /**
     * Save a bitmap to the faces folder
     * @param context Application context
     * @param bitmap The bitmap to save
     * @param studentId The student ID to use as filename
     * @return The file path if successful, null if failed
     */
    fun saveFacePhoto(context: Context, bitmap: Bitmap, studentId: String): String? {
        return try {
            val facesDir = getFacesDirectory(context)
            val fileName = "${studentId}_${System.currentTimeMillis()}.jpg"
            val file = File(facesDir, fileName)
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            Log.d(TAG, "Photo saved: ${file.absolutePath}")
            file.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save photo", e)
            null
        }
    }
    
    /**
     * Load a bitmap from a file path
     * @param filePath The absolute file path
     * @return The bitmap if successful, null if failed
     */
    fun loadFacePhoto(filePath: String): Bitmap? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(filePath)
            } else {
                Log.w(TAG, "Photo file not found: $filePath")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load photo: $filePath", e)
            null
        }
    }
    
    /**
     * Load a bitmap from URI (for gallery selection)
     * @param context Application context
     * @param uri The URI of the selected image
     * @return The bitmap if successful, null if failed
     */
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bitmap from URI: $uri", e)
            null
        }
    }
    
    /**
     * Delete a face photo
     * @param filePath The absolute file path to delete
     * @return true if successful, false if failed
     */
    fun deleteFacePhoto(filePath: String?): Boolean {
        return try {
            if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) {
                    val deleted = file.delete()
                    Log.d(TAG, "Photo deleted: $filePath, success: $deleted")
                    deleted
                } else {
                    Log.w(TAG, "Photo file not found for deletion: $filePath")
                    true // Consider it successful if file doesn't exist
                }
            } else {
                true // No file to delete
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete photo: $filePath", e)
            false
        }
    }
    
    /**
     * Get the file size of a photo in bytes
     * @param filePath The absolute file path
     * @return The file size in bytes, or 0 if file doesn't exist
     */
    fun getPhotoFileSize(filePath: String?): Long {
        return try {
            if (filePath != null) {
                val file = File(filePath)
                if (file.exists()) file.length() else 0L
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get photo file size: $filePath", e)
            0L
        }
    }
    
    /**
     * Clean up old photos for a student (when updating with new photo)
     * @param context Application context
     * @param studentId The student ID
     * @param keepFilePath The file path to keep (current photo)
     */
    fun cleanupOldPhotos(context: Context, studentId: String, keepFilePath: String?) {
        try {
            val facesDir = getFacesDirectory(context)
            val files = facesDir.listFiles { file ->
                file.name.startsWith("${studentId}_") && 
                file.absolutePath != keepFilePath
            }
            
            files?.forEach { file ->
                val deleted = file.delete()
                Log.d(TAG, "Cleaned up old photo: ${file.name}, success: $deleted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup old photos for student: $studentId", e)
        }
    }
    
    /**
     * Resize bitmap to a maximum dimension while maintaining aspect ratio
     * @param bitmap The original bitmap
     * @param maxDimension The maximum width or height
     * @return The resized bitmap
     */
    fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }
        
        val ratio = if (width > height) {
            maxDimension.toFloat() / width
        } else {
            maxDimension.toFloat() / height
        }
        
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return bitmap.scale(newWidth, newHeight)
    }
}
