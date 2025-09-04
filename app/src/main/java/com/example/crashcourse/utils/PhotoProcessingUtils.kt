package com.example.crashcourse.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.core.graphics.scale
import android.util.Log
import com.example.crashcourse.ml.BitmapUtils
import com.example.crashcourse.ml.FaceRecognizer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

object PhotoProcessingUtils {
    private const val TAG = "PhotoProcessingUtils"
    private const val INPUT_SIZE = 160

    /**
     * Process a bitmap to detect faces and generate embeddings
     * @param context Application context
     * @param bitmap The bitmap to process
     * @return Pair of (processed bitmap, embedding) if face detected, null otherwise
     */
    suspend fun processBitmapForFaceEmbedding(
        @Suppress("UNUSED_PARAMETER") context: Context,
        bitmap: Bitmap
    ): Pair<Bitmap, FloatArray>? = withContext(Dispatchers.IO) {
        try {
            // Resize bitmap if too large for better performance
            val processedBitmap = if (bitmap.width > 1024 || bitmap.height > 1024) {
                PhotoStorageUtils.resizeBitmap(bitmap, 1024)
            } else {
                bitmap
            }

            // Detect faces in the bitmap
            val faces = detectFacesInBitmap(processedBitmap)

            if (faces.isEmpty()) {
                Log.w(TAG, "No faces detected in the image")
                return@withContext null
            }

            // Use the largest face (most prominent)
            val largestFace = faces.maxByOrNull { it.width() * it.height() }
                ?: return@withContext null

            Log.d(TAG, "Processing face at: $largestFace")

            // Crop and process the face
            val faceBitmap = cropFaceFromBitmap(processedBitmap, largestFace)
            val embedding = generateEmbeddingFromFaceBitmap(faceBitmap)

            Log.d(TAG, "Successfully generated embedding with ${embedding.size} dimensions")

            Pair(faceBitmap, embedding)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to process bitmap for face embedding", e)
            null
        }
    }

    /**
     * Detect faces in a bitmap using ML Kit
     * @param bitmap The bitmap to analyze
     * @return List of face bounding boxes
     */
    private suspend fun detectFacesInBitmap(bitmap: Bitmap): List<Rect> =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            val detector = FaceDetection.getClient(
                FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                    .build()
            )

            detector.process(image)
                .addOnSuccessListener { faces ->
                    val faceRects = faces.map { it.boundingBox }
                    Log.d(TAG, "Detected ${faceRects.size} faces")
                    continuation.resume(faceRects)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Face detection failed", e)
                    continuation.resume(emptyList())
                }
        }

    /**
     * Crop face from bitmap based on bounding box
     * @param bitmap The original bitmap
     * @param faceRect The face bounding box
     * @return Cropped face bitmap
     */
    private fun cropFaceFromBitmap(bitmap: Bitmap, faceRect: Rect): Bitmap {
        // Add some padding around the face
        val padding = 0.2f
        val paddingX = (faceRect.width() * padding).toInt()
        val paddingY = (faceRect.height() * padding).toInt()

        val left = (faceRect.left - paddingX).coerceAtLeast(0)
        val top = (faceRect.top - paddingY).coerceAtLeast(0)
        val right = (faceRect.right + paddingX).coerceAtMost(bitmap.width)
        val bottom = (faceRect.bottom + paddingY).coerceAtMost(bitmap.height)

        val width = right - left
        val height = bottom - top

        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }

    /**
     * Generate embedding from a face bitmap
     * @param faceBitmap The cropped face bitmap
     * @return Face embedding as FloatArray
     */
    private fun generateEmbeddingFromFaceBitmap(faceBitmap: Bitmap): FloatArray {
        // Resize to model input size
        val resizedBitmap = faceBitmap.scale(INPUT_SIZE, INPUT_SIZE)

        // Convert to ByteBuffer format expected by the model
        val buffer = bitmapToByteBuffer(resizedBitmap)

        // Generate embedding using FaceRecognizer
        return FaceRecognizer.recognizeFace(buffer)
    }

    /**
     * Convert bitmap to ByteBuffer for model input
     * @param bitmap The bitmap to convert
     * @return ByteBuffer ready for model inference
     */
    private fun bitmapToByteBuffer(bitmap: Bitmap): java.nio.ByteBuffer {
        val buffer = java.nio.ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * 4)
            .order(java.nio.ByteOrder.nativeOrder())

        val intVals = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(intVals, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        var idx = 0
        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val pixel = intVals[idx++]
                // Normalize to [-1, 1] range
                buffer.putFloat(((pixel shr 16 and 0xFF) - 127.5f) / 128f) // R
                buffer.putFloat(((pixel shr 8  and 0xFF) - 127.5f) / 128f) // G
                buffer.putFloat(((pixel       and 0xFF) - 127.5f) / 128f) // B
            }
        }
        buffer.rewind()
        return buffer
    }

    /**
     * Validate if a bitmap contains a clear face
     * @param bitmap The bitmap to validate
     * @return true if contains a clear face, false otherwise
     */
    suspend fun validateFaceInBitmap(bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        try {
            val faces = detectFacesInBitmap(bitmap)
            val hasValidFace = faces.isNotEmpty() && faces.any { face ->
                // Check if face is large enough (at least 50x50 pixels)
                face.width() >= 50 && face.height() >= 50
            }
            Log.d(TAG, "Face validation result: $hasValidFace (${faces.size} faces detected)")
            hasValidFace
        } catch (e: Exception) {
            Log.e(TAG, "Face validation failed", e)
            false
        }
    }

    /**
     * Get face confidence score (0.0 to 1.0)
     * @param bitmap The bitmap to analyze
     * @return Confidence score, or 0.0 if no face detected
     */
    suspend fun getFaceConfidence(bitmap: Bitmap): Float = withContext(Dispatchers.IO) {
        try {
            val faces = detectFacesInBitmap(bitmap)
            if (faces.isEmpty()) return@withContext 0.0f

            val largestFace = faces.maxByOrNull { it.width() * it.height() }
                ?: return@withContext 0.0f

            // Calculate confidence based on face size relative to image
            val faceArea = largestFace.width() * largestFace.height()
            val imageArea = bitmap.width * bitmap.height
            val sizeRatio = faceArea.toFloat() / imageArea.toFloat()

            // Confidence increases with face size, capped at 1.0
            (sizeRatio * 10).coerceAtMost(1.0f)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate face confidence", e)
            0.0f
        }
    }
}
