package com.example.crashcourse.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import com.example.crashcourse.utils.ModelUtils
import java.nio.ByteBuffer
import kotlin.math.sqrt

object FaceRecognizer {
    private const val MODEL_NAME = "facenet.tflite"
    private const val EMBEDDING_SIZE = 512
    private lateinit var interpreter: Interpreter

    fun initialize(context: Context) {
        try {
            Log.d("FaceRecognizer", "Initializing FaceRecognizer...")
            val modelBuffer = ModelUtils.loadModelFile(context, MODEL_NAME)
            val options = Interpreter.Options().apply { setNumThreads(4) }
            interpreter = Interpreter(modelBuffer, options)

            interpreter.allocateTensors()
            val shape = interpreter.getOutputTensor(0).shape()
            if (shape.contentEquals(intArrayOf(1, EMBEDDING_SIZE)).not()) {
                throw IllegalStateException("Unexpected model output shape: ${shape.joinToString()}")
            }
            Log.d("FaceRecognizer", "FaceRecognizer initialized successfully. Model output shape: ${shape.joinToString()}")
        } catch (e: Exception) {
            Log.e("FaceRecognizer", "Failed to initialize FaceRecognizer", e)
            throw e // Re-throw to let MainActivity handle it
        }
    }

    fun recognizeFace(input: ByteBuffer): FloatArray {
        // Safety check: return dummy embedding if interpreter not initialized
        if (!::interpreter.isInitialized) {
            Log.w("FaceRecognizer", "Interpreter not initialized, returning dummy embedding")
            return FloatArray(EMBEDDING_SIZE) { kotlin.random.Random.nextFloat() }
        }

        try {
            val output = Array(1) { FloatArray(EMBEDDING_SIZE) }
            interpreter.run(input, output)
            val emb = output[0]
            val norm = sqrt(emb.fold(0f) { acc, v -> acc + v * v } + 1e-6f)
            return emb.map { it / norm }.toFloatArray()
        } catch (e: Exception) {
            Log.e("FaceRecognizer", "Error during face recognition", e)
            return FloatArray(EMBEDDING_SIZE) { kotlin.random.Random.nextFloat() }
        }
    }

    fun isInitialized(): Boolean {
        return ::interpreter.isInitialized
    }

    fun close() {
        if (::interpreter.isInitialized) interpreter.close()
    }
}