package com.example.crashcourse.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.crashcourse.utils.PhotoProcessingUtils
import com.example.crashcourse.viewmodel.FaceViewModel
import com.example.crashcourse.db.FaceEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 📷 RealPhotoCaptureManager - Standalone photo capture system
 * 
 * Features:
 * - ✅ Independent of FaceScanner/FaceAnalyzer
 * - ✅ Real camera photo capture using CameraX ImageCapture
 * - ✅ Asynchronous processing with coroutines
 * - ✅ Automatic face embedding generation
 * - ✅ Database integration with photo path storage
 * - ✅ UI refresh callbacks
 * - ✅ Error handling and logging
 */
open class RealPhotoCaptureManager(
    protected val context: Context,
    protected val lifecycleOwner: LifecycleOwner
) {
    companion object {
        private const val TAG = "RealPhotoCaptureManager"
        private const val IMAGE_QUALITY = 95
    }

    protected var imageCapture: ImageCapture? = null
    protected var cameraProvider: ProcessCameraProvider? = null
    protected var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    protected val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Camera state management
    protected var currentPreviewView: PreviewView? = null
    var currentCameraSelector: CameraSelector? = null

    // TextToSpeech for voice feedback
    protected var textToSpeech: TextToSpeech? = null
    protected var isTtsReady = false

    /**
     * Initialize camera for photo capture
     */
    suspend fun initializeCamera(
        previewView: PreviewView,
        useBackCamera: Boolean = true
    ): Boolean = withContext(Dispatchers.Main) {
        try {
            Log.d(TAG, "Initializing camera for photo capture...")

            // Store preview view reference
            currentPreviewView = previewView

            // Initialize TextToSpeech
            initializeTextToSpeech()

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()

            // Set initial camera selector
            currentCameraSelector = if (useBackCamera) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            // Bind camera with initial settings
            bindCamera()

            Log.d(TAG, "Camera initialized successfully with ${if (useBackCamera) "back" else "front"} camera")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize camera", e)
            false
        }
    }

    /**
     * Switch camera between front and back
     */
    suspend fun switchCamera(useBackCamera: Boolean): Boolean = withContext(Dispatchers.Main) {
        try {
            Log.d(TAG, "Switching to ${if (useBackCamera) "back" else "front"} camera...")

            // Update camera selector
            currentCameraSelector = if (useBackCamera) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            // Rebind camera with new selector
            bindCamera()

            Log.d(TAG, "Camera switched successfully to ${if (useBackCamera) "back" else "front"} camera")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Error switching camera", e)
            false
        }
    }

    /**
     * Bind camera with current selector and preview view
     */
    private fun bindCamera() {
        val previewView = currentPreviewView ?: return
        val cameraSelector = currentCameraSelector ?: return
        val provider = cameraProvider ?: return

        try {
            // Preview use case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // ImageCapture use case for taking photos
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            // Bind use cases to camera
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error binding camera", e)
        }
    }

    /**
     * Capture photo and process it asynchronously
     */
    suspend fun captureAndProcessPhoto(
        studentId: String,
        name: String,
        viewModel: FaceViewModel,
        className: String = "",
        subClass: String = "",
        grade: String = "",
        subGrade: String = "",
        program: String = "",
        role: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
        onProgress: (String) -> Unit = {}
    ) {
        coroutineScope.launch {
            try {
                Log.d(TAG, "=== STARTING REAL PHOTO CAPTURE ===")
                onProgress("Capturing photo...")
                
                // Step 1: Capture photo
                val photoFile = capturePhotoToFile(studentId)
                if (photoFile == null) {
                    speakText("Photo capture failed")
                    onError("Failed to capture photo")
                    return@launch
                }
                
                Log.d(TAG, "Photo captured: ${photoFile.absolutePath}")
                onProgress("Processing image...")
                
                // Step 2: Load and process bitmap
                val bitmap = loadAndProcessBitmap(photoFile)
                if (bitmap == null) {
                    speakText("Image processing failed")
                    onError("Failed to process captured image")
                    return@launch
                }
                
                Log.d(TAG, "Bitmap processed: ${bitmap.width}x${bitmap.height}")
                onProgress("Generating face embedding...")
                
                // Step 3: Generate face embedding
                val embedding = generateFaceEmbedding(bitmap)
                if (embedding == null) {
                    speakText("No face detected in image")
                    onError("No face detected in captured image")
                    return@launch
                }
                
                Log.d(TAG, "Face embedding generated: ${embedding.size} values")
                onProgress("Saving to database...")
                
                // Step 4: Save to database
                withContext(Dispatchers.Main) {
                    viewModel.registerFace(
                        studentId = studentId,
                        name = name,
                        embedding = embedding,
                        photoUrl = photoFile.absolutePath,
                        className = className,
                        subClass = subClass,
                        grade = grade,
                        subGrade = subGrade,
                        program = program,
                        role = role,
                        onSuccess = {
                            Log.d(TAG, "✅ Real photo registration completed successfully!")

                            // Voice feedback for successful registration
                            speakText("Registration successful! Welcome, $name!")

                            onSuccess()
                        },
                        onDuplicate = { existingName ->
                            Log.d(TAG, "⚠️ Duplicate face detected: $existingName")

                            // Voice feedback for duplicate detection
                            speakText("Face already registered as $existingName")

                            onError("Face already registered as: $existingName")
                        }
                    )
                }
                
                Log.d(TAG, "=== REAL PHOTO CAPTURE COMPLETED ===")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in photo capture process", e)
                onError("Photo capture failed: ${e.message}")
            }
        }
    }

    /**
     * Capture photo to file using ImageCapture
     */
    protected suspend fun capturePhotoToFile(studentId: String): File? {
        return try {
            // Create faces directory
            val facesDir = File(context.filesDir, "faces")
            if (!facesDir.exists()) {
                facesDir.mkdirs()
            }

            // Create photo file
            val photoFile = File(facesDir, "face_${studentId}.jpg")

            // Create output file options
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            // Capture photo
            val imageCapture = this@RealPhotoCaptureManager.imageCapture
                ?: throw IllegalStateException("ImageCapture not initialized")

            // Use CompletableDeferred for simpler async handling
            val deferred = CompletableDeferred<File?>()

            imageCapture.takePicture(
                outputFileOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        Log.d(TAG, "Photo saved: ${photoFile.absolutePath}")
                        deferred.complete(photoFile)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed", exception)
                        deferred.complete(null)
                    }
                }
            )

            // Wait for the result
            deferred.await()

        } catch (e: Exception) {
            Log.e(TAG, "Error capturing photo to file", e)
            null
        }
    }

    /**
     * Load and process bitmap from file
     */
    protected suspend fun loadAndProcessBitmap(photoFile: File): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            
            // Resize if too large (optional optimization)
            val maxSize = 1024
            if (bitmap.width > maxSize || bitmap.height > maxSize) {
                val scale = minOf(
                    maxSize.toFloat() / bitmap.width,
                    maxSize.toFloat() / bitmap.height
                )
                
                val matrix = Matrix().apply {
                    postScale(scale, scale)
                }
                
                val resizedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                
                // Save resized bitmap back to file
                FileOutputStream(photoFile).use { out ->
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, out)
                }
                
                bitmap.recycle()
                resizedBitmap
            } else {
                bitmap
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap from file", e)
            null
        }
    }

    /**
     * Generate face embedding from bitmap
     */
    protected suspend fun generateFaceEmbedding(bitmap: Bitmap): FloatArray? = withContext(Dispatchers.IO) {
        try {
            // Use existing PhotoProcessingUtils to process bitmap and generate embedding
            val result = PhotoProcessingUtils.processBitmapForFaceEmbedding(context, bitmap)
            result?.second // Return the embedding part of the Pair
        } catch (e: Exception) {
            Log.e(TAG, "Error generating face embedding", e)
            null
        }
    }

    /**
     * Initialize TextToSpeech for voice feedback
     */
    protected fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(java.util.Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported for TTS")
                } else {
                    isTtsReady = true
                    Log.d(TAG, "TextToSpeech initialized successfully")
                }
            } else {
                Log.e(TAG, "TextToSpeech initialization failed")
            }
        }
    }

    /**
     * Speak text using TextToSpeech
     */
    protected fun speakText(text: String) {
        if (isTtsReady && textToSpeech != null) {
            Log.d(TAG, "Speaking: $text")
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.w(TAG, "TextToSpeech not ready, cannot speak: $text")
        }
    }

    /**
     * Release camera resources
     */
    fun release() {
        Log.d(TAG, "Releasing camera resources...")
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
        coroutineScope.cancel()

        // Release TextToSpeech
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isTtsReady = false
    }
}

/**
 * 📷 EditUserPhotoCaptureManager - Specialized manager for updating existing user photos
 *
 * Features:
 * - ✅ Extends RealPhotoCaptureManager functionality
 * - ✅ Updates existing face records instead of creating new ones
 * - ✅ Preserves user data while updating photo and embedding
 * - ✅ Voice feedback for successful updates
 */
class EditUserPhotoCaptureManager(
    context: Context,
    lifecycleOwner: LifecycleOwner
) : RealPhotoCaptureManager(context, lifecycleOwner) {

    /**
     * Capture photo and update existing user
     */
    suspend fun captureAndUpdateUserPhoto(
        user: FaceEntity,
        viewModel: FaceViewModel,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
        onProgress: (String) -> Unit = {}
    ) {
        coroutineScope.launch {
            try {
                Log.d(TAG, "=== STARTING USER PHOTO UPDATE ===")
                Log.d(TAG, "Updating photo for user: ${user.name} (${user.studentId})")
                onProgress("Capturing photo...")

                // Step 1: Capture photo
                val photoFile = capturePhotoToFile(user.studentId)
                if (photoFile == null) {
                    speakText("Photo capture failed")
                    onError("Failed to capture photo")
                    return@launch
                }

                Log.d(TAG, "Photo captured: ${photoFile.absolutePath}")
                onProgress("Processing image...")

                // Step 2: Load and process bitmap
                val bitmap = loadAndProcessBitmap(photoFile)
                if (bitmap == null) {
                    speakText("Image processing failed")
                    onError("Failed to process captured image")
                    return@launch
                }

                Log.d(TAG, "Bitmap processed: ${bitmap.width}x${bitmap.height}")
                onProgress("Generating face embedding...")

                // Step 3: Generate face embedding
                val embedding = generateFaceEmbedding(bitmap)
                if (embedding == null) {
                    speakText("No face detected in image")
                    onError("No face detected in captured image")
                    return@launch
                }

                Log.d(TAG, "Face embedding generated: ${embedding.size} values")
                onProgress("Updating user record...")

                // Step 4: Update existing user with new photo and embedding
                withContext(Dispatchers.Main) {
                    viewModel.updateFaceWithPhoto(
                        face = user, // Keep existing user data
                        photoBitmap = bitmap,
                        embedding = embedding,
                        onComplete = {
                            Log.d(TAG, "✅ User photo updated successfully!")

                            // Voice feedback for successful update
                            speakText("Photo updated successfully for ${user.name}!")

                            onSuccess()
                        },
                        onError = { error ->
                            Log.e(TAG, "❌ Failed to update user photo: $error")
                            speakText("Update failed")
                            onError("Failed to update user: $error")
                        }
                    )
                }

                Log.d(TAG, "=== USER PHOTO UPDATE COMPLETED ===")

            } catch (e: Exception) {
                Log.e(TAG, "Error in user photo update process", e)
                speakText("Update failed")
                onError("Photo update failed: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "EditUserPhotoCaptureManager"
    }
}
