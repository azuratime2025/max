package com.example.crashcourse.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 🗂️ FaceImageSaver - Clean separation for face image storage
 *
 * Benefits:
 * - 🔒 Clean separation of concerns
 * - ♻️ Reusable for cloud uploads later
 * - 🚀 Perfect for background saving with coroutines
 * - 📁 Organized face storage management
 *
 * Example Usage:
 * ```kotlin
 * val faceBitmap = BitmapUtils.extractFaceBitmap(image, boundingBox, rotation)
 *
 * CoroutineScope(Dispatchers.IO).launch {
 *     val path = FaceImageSaver.saveToFacesFolder(context, faceBitmap)
 *
 *     path?.let {
 *         withContext(Dispatchers.Main) {
 *             generateEmbedding(it) // ← your untouched embedding code
 *         }
 *     }
 * }
 * ```
 */
object FaceImageSaver {

    private const val TAG = "FaceImageSaver"
    private const val FACES_FOLDER = "faces"
    private const val IMAGE_QUALITY = 90 // JPEG quality (0-100)

    /**
     * Save a face bitmap to the local faces folder
     *
     * @param context Android context for file access
     * @param bitmap The face bitmap to save
     * @return File path if successful, null if failed
     */
    fun saveToFacesFolder(context: Context, bitmap: Bitmap): String? {
        return try {
            Log.d(TAG, "Starting to save face image...")

            // Create faces folder if it doesn't exist
            val folder = File(context.filesDir, FACES_FOLDER)
            if (!folder.exists()) {
                val created = folder.mkdirs()
                Log.d(TAG, "Faces folder created: $created at ${folder.absolutePath}")
            } else {
                Log.d(TAG, "Faces folder already exists at ${folder.absolutePath}")
            }

            // Generate unique filename with timestamp
            val timestamp = System.currentTimeMillis()
            val fileName = "face_$timestamp.jpg"
            val file = File(folder, fileName)

            Log.d(TAG, "Saving image to: ${file.absolutePath}")
            Log.d(TAG, "Bitmap size: ${bitmap.width}x${bitmap.height}")

            // Save bitmap to file with proper resource management
            FileOutputStream(file).use { outputStream ->
                val compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outputStream)
                outputStream.flush()

                if (compressed) {
                    Log.d(TAG, "Image saved successfully: ${file.absolutePath}")
                    Log.d(TAG, "File size: ${file.length()} bytes")
                    return file.absolutePath
                } else {
                    Log.e(TAG, "Failed to compress bitmap")
                    return null
                }
            }

        } catch (e: IOException) {
            Log.e(TAG, "IOException while saving face image", e)
            null
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while saving face image", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while saving face image", e)
            null
        }
    }

    /**
     * Get the faces folder directory
     *
     * @param context Android context
     * @return File object representing the faces folder
     */
    fun getFacesFolder(context: Context): File {
        return File(context.filesDir, "faces")
    }

    /**
     * Check if faces folder exists
     *
     * @param context Android context
     * @return true if folder exists, false otherwise
     */
    fun facesFolderExists(context: Context): Boolean {
        val exists = getFacesFolder(context).exists()
        Log.d(TAG, "Faces folder exists: $exists")
        return exists
    }

    /**
     * Get the number of saved face images
     *
     * @param context Android context
     * @return Number of face images in the folder
     */
    fun getFaceImageCount(context: Context): Int {
        return try {
            val facesFolder = getFacesFolder(context)
            if (facesFolder.exists()) {
                val count = facesFolder.listFiles()?.size ?: 0
                Log.d(TAG, "Face images count: $count")
                count
            } else {
                Log.d(TAG, "Faces folder doesn't exist, count: 0")
                0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error counting face images", e)
            0
        }
    }

    /**
     * Delete a face image file
     *
     * @param filePath Absolute path to the image file
     * @return true if deleted successfully, false otherwise
     */
    fun deleteFaceImage(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                val deleted = file.delete()
                Log.d(TAG, "Face image deleted: $deleted - $filePath")
                deleted
            } else {
                Log.w(TAG, "Face image file doesn't exist: $filePath")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting face image: $filePath", e)
            false
        }
    }

    /**
     * Save face image with simplified interface
     *
     * @param context Android context for file access
     * @param bitmap The face bitmap to save
     * @return Absolute file path of saved image
     */
    fun saveFaceImage(context: Context, bitmap: Bitmap): String {
        // 1. Create folder /files/faces if it doesn't exist
        val facesDir = File(context.filesDir, "faces")
        if (!facesDir.exists()) facesDir.mkdirs()

        // 2. Create a new image file with timestamp name
        val imageFile = File(facesDir, "face_${System.currentTimeMillis()}.jpg")

        // 3. Save the bitmap to that file
        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        // 4. Log the saved path
        Log.d("PHOTO_SAVE", "Saved photo at: ${imageFile.absolutePath}")

        // 5. Return imageFile.absolutePath
        return imageFile.absolutePath
    }

    /**
     * Save face image with student ID as filename
     *
     * @param context Android context for file access
     * @param bitmap The face bitmap to save
     * @param studentId Student ID to use as filename
     * @return Absolute file path of saved image
     */
    fun saveFaceImageWithStudentId(context: Context, bitmap: Bitmap, studentId: String): String {
        // 1. Create folder /files/faces if it doesn't exist
        val facesDir = File(context.filesDir, "faces")
        if (!facesDir.exists()) {
            val created = facesDir.mkdirs()
            Log.d(TAG, "Faces folder created: $created at ${facesDir.absolutePath}")
        }

        // 2. Create image file with student ID as filename
        val imageFile = File(facesDir, "face_${studentId}.jpg")

        // 3. Save the bitmap to that file
        try {
            FileOutputStream(imageFile).use { outputStream ->
                val compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()

                if (compressed) {
                    Log.d(TAG, "Face image saved for student $studentId: ${imageFile.absolutePath}")
                    Log.d(TAG, "File size: ${imageFile.length()} bytes")
                } else {
                    Log.e(TAG, "Failed to compress bitmap for student $studentId")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving face image for student $studentId", e)
            throw e
        }

        // 4. Return absolute file path
        return imageFile.absolutePath
    }
}
