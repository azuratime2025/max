package com.example.crashcourse.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.example.crashcourse.ui.components.FaceViewModel
import com.example.crashcourse.utils.FaceImageSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * FaceRegistrationHelper - Helper class for complete face registration with image saving
 * 
 * This helper demonstrates how to integrate face image saving with the registration process.
 * It provides utilities for capturing face images and saving them with student IDs.
 */
object FaceRegistrationHelper {
    
    private const val TAG = "FaceRegistrationHelper"
    
    /**
     * Complete face registration with image saving
     * 
     * @param context Android context
     * @param studentId Student ID for filename
     * @param name Student name
     * @param embedding Face embedding
     * @param faceBitmap Captured face bitmap (if available)
     * @param boundingBox Face bounding box (if available)
     * @param viewModel FaceViewModel for registration
     * @param onSuccess Success callback
     * @param onDuplicate Duplicate detection callback
     * @param onError Error callback
     */
    suspend fun registerFaceWithImage(
        context: Context,
        studentId: String,
        name: String,
        embedding: FloatArray,
        faceBitmap: Bitmap? = null,
        boundingBox: Rect? = null,
        viewModel: FaceViewModel,
        className: String = "",
        subClass: String = "",
        grade: String = "",
        subGrade: String = "",
        program: String = "",
        role: String = "",
        onSuccess: () -> Unit,
        onDuplicate: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Log.d(TAG, "Starting face registration with image for student: $studentId")
            
            // Save face image if available
            val photoUrl = if (faceBitmap != null) {
                Log.d(TAG, "Saving face image for student: $studentId")

                // Note: If you need to crop the face from a larger bitmap using boundingBox,
                // you would need to implement bitmap cropping logic here.
                // For now, we'll save the provided bitmap as-is.
                val faceImage = faceBitmap

                // Save the face image with student ID
                FaceImageSaver.saveFaceImageWithStudentId(context, faceImage, studentId)
            } else {
                Log.d(TAG, "No face image provided for student: $studentId")
                null
            }
            
            Log.d(TAG, "Photo URL: $photoUrl")
            
            // Register face with photo URL
            withContext(Dispatchers.Main) {
                viewModel.registerFace(
                    studentId = studentId,
                    name = name,
                    embedding = embedding,
                    photoUrl = photoUrl,
                    className = className,
                    subClass = subClass,
                    grade = grade,
                    subGrade = subGrade,
                    program = program,
                    role = role,
                    onSuccess = {
                        Log.d(TAG, "Face registration successful for: $name")
                        onSuccess()
                    },
                    onDuplicate = { existingName ->
                        Log.d(TAG, "Duplicate face detected: $existingName")
                        onDuplicate(existingName)
                    }
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during face registration with image", e)
            withContext(Dispatchers.Main) {
                onError("Registration failed: ${e.message}")
            }
        }
    }
    
    /**
     * Update existing face with new image
     * 
     * @param context Android context
     * @param studentId Student ID
     * @param faceBitmap New face bitmap
     * @param embedding New face embedding
     * @param viewModel FaceViewModel for update
     * @param onSuccess Success callback
     * @param onError Error callback
     */
    suspend fun updateFaceImage(
        context: Context,
        studentId: String,
        faceBitmap: Bitmap,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Log.d(TAG, "Updating face image for student: $studentId")
            
            // Save new face image
            val photoUrl = FaceImageSaver.saveFaceImageWithStudentId(context, faceBitmap, studentId)
            Log.d(TAG, "New photo URL: $photoUrl")
            
            // Find existing face entity
            // Note: This would require adding a method to FaceViewModel to get face by student ID
            // For now, this is a placeholder for the update logic
            
            withContext(Dispatchers.Main) {
                onSuccess()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating face image", e)
            withContext(Dispatchers.Main) {
                onError("Update failed: ${e.message}")
            }
        }
    }
    
    /**
     * Validate face image before saving
     * 
     * @param bitmap Face bitmap to validate
     * @return true if valid, false otherwise
     */
    fun validateFaceImage(bitmap: Bitmap?): Boolean {
        if (bitmap == null) {
            Log.w(TAG, "Face bitmap is null")
            return false
        }
        
        if (bitmap.isRecycled) {
            Log.w(TAG, "Face bitmap is recycled")
            return false
        }
        
        if (bitmap.width < 50 || bitmap.height < 50) {
            Log.w(TAG, "Face bitmap too small: ${bitmap.width}x${bitmap.height}")
            return false
        }
        
        Log.d(TAG, "Face bitmap validation passed: ${bitmap.width}x${bitmap.height}")
        return true
    }
    
    /**
     * Generate student ID if not provided
     * 
     * @return Generated student ID
     */
    fun generateStudentId(): String {
        return "STU${System.currentTimeMillis()}"
    }
    
    /**
     * Get expected photo path for a student ID
     * 
     * @param context Android context
     * @param studentId Student ID
     * @return Expected photo path
     */
    fun getExpectedPhotoPath(context: Context, studentId: String): String {
        val facesDir = FaceImageSaver.getFacesFolder(context)
        return "${facesDir.absolutePath}/face_${studentId}.jpg"
    }
    
    /**
     * Check if face image exists for student
     * 
     * @param context Android context
     * @param studentId Student ID
     * @return true if image exists, false otherwise
     */
    fun faceImageExists(context: Context, studentId: String): Boolean {
        val expectedPath = getExpectedPhotoPath(context, studentId)
        val exists = java.io.File(expectedPath).exists()
        Log.d(TAG, "Face image exists for $studentId: $exists")
        return exists
    }
}

/**
 * Usage Examples:
 * 
 * // Complete registration with image
 * scope.launch(Dispatchers.IO) {
 *     FaceRegistrationHelper.registerFaceWithImage(
 *         context = context,
 *         studentId = studentId,
 *         name = name,
 *         embedding = embedding,
 *         faceBitmap = capturedBitmap,
 *         boundingBox = faceRect,
 *         viewModel = viewModel,
 *         onSuccess = { /* success */ },
 *         onDuplicate = { existingName -> /* duplicate */ },
 *         onError = { error -> /* error */ }
 *     )
 * }
 * 
 * // Check if image exists
 * val hasImage = FaceRegistrationHelper.faceImageExists(context, studentId)
 * 
 * // Get expected path
 * val photoPath = FaceRegistrationHelper.getExpectedPhotoPath(context, studentId)
 */
