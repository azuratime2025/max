// FaceAnalyzer2.kt
package com.example.crashcourse.ml

import android.graphics.Rect
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.atomic.AtomicBoolean
import com.example.crashcourse.ml.FaceRecognizer
import com.example.crashcourse.ml.BitmapUtils

@Suppress("UnsafeOptInUsageError")
class FaceAnalyzer2(
    private val onFaceEmbedding: (Rect, FloatArray) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .enableTracking()
            .build()
    )

    // Compose-observable face boxes
    var faceBounds by mutableStateOf<List<Rect>>(emptyList())
        private set

    // Raw image dimensions with rotation handling
    var imageSize by mutableStateOf(IntSize(0, 0))
        private set

    // Track rotation for proper dimension calculation
    var imageRotation by mutableStateOf(0)
        private set

    private val isProcessing = AtomicBoolean(false)
    private var lastProcessTime = 0L

    override fun analyze(imageProxy: ImageProxy) {
        // Skip frames if processing is taking too long (throttle to ~10 FPS)
        val currentTime = System.currentTimeMillis()
        if (isProcessing.get() || (currentTime - lastProcessTime < 100)) {
            imageProxy.close()
            return
        }
        isProcessing.set(true)
        lastProcessTime = currentTime

        val mediaImage: Image? = imageProxy.image
        if (mediaImage != null) {
            val rotation = imageProxy.imageInfo.rotationDegrees
            imageRotation = rotation
            
            // Calculate dimensions considering rotation
            imageSize = if (rotation % 180 == 0) {
                IntSize(imageProxy.width, imageProxy.height)
            } else {
                IntSize(imageProxy.height, imageProxy.width)
            }

            // ML Kit face detection
            val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    faceBounds = faces.map { it.boundingBox }

                    for (face in faces) {
                        // Crop + preprocess + infer embedding
                        val buffer = BitmapUtils.preprocessFace(
                            image       = mediaImage,
                            boundingBox = face.boundingBox,
                            rotation    = rotation
                        )
                        val embedding = FaceRecognizer.recognizeFace(buffer)
                        onFaceEmbedding(face.boundingBox, embedding)
                    }
                    Log.d("FaceAnalyzer2", "Detected ${faces.size} faces | Size: $imageSize | Rot: ${rotation}°")
                }
                .addOnFailureListener { e ->
                    Log.e("FaceAnalyzer2", "Detection failed", e)
                    faceBounds = emptyList()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                    isProcessing.set(false)
                }
        } else {
            imageProxy.close()
            isProcessing.set(false)
        }
    }

    fun close() {
        detector.close()
    }
}