package com.example.crashcourse.ml

import android.content.Context
import com.example.crashcourse.db.FaceCache
import com.example.crashcourse.db.FaceEntity
import com.example.crashcourse.utils.cosineDistance
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class FaceRecognitionHelper(private val context: Context) {

    /**
     * Loads a TFLite model file from assets.
     */
    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelName)
        FileInputStream(assetFileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.declaredLength
            )
        }
    }

    // Add your TFLite interpreter, input/output buffer handling, and embedding methods here.
    // For example: fun getFaceEmbedding(bitmap: Bitmap): FloatArray { ... }

    /**
     * Finds the best match for the given face embedding from the in-memory cache.
     *
     * @param embedding The face embedding to match.
     * @return Pair of (FaceEntity? if match found, cosine distance).
     */
    suspend fun findBestMatch(embedding: FloatArray): Pair<FaceEntity?, Float> {
        val cachedList = FaceCache.load(context) // List<Pair<String, FloatArray>>
        var bestName: String? = null
        var bestEmbedding: FloatArray? = null
        var bestDistance = Float.MAX_VALUE

        for ((name, existingEmbedding) in cachedList) {
            val distance = cosineDistance(existingEmbedding, embedding)
            if (distance < bestDistance) {
                bestDistance = distance
                bestName = name
                bestEmbedding = existingEmbedding
            }
        }

        return if (bestName != null && bestEmbedding != null) {
            // Create a temporary FaceEntity for matching purposes
            FaceEntity(
                studentId = "temp_" + System.currentTimeMillis(),
                name = bestName,
                embedding = bestEmbedding,
                timestamp = 0L
            ) to bestDistance
        } else {
            null to bestDistance
        }
    }
}
