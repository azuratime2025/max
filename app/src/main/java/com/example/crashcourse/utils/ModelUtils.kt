package com.example.crashcourse.utils

import android.content.Context
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object ModelUtils {
    /**
     * Loads a TensorFlow Lite model file from the assets folder.
     *
     * @param context The application context.
     * @param modelName The name of the model file, e.g., "facenet.tflite".
     * @return A MappedByteBuffer containing the model data.
     */
    fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        context.assets.openFd(modelName).use { fd ->
            FileInputStream(fd.fileDescriptor).use { stream ->
                val channel = stream.channel
                return channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fd.startOffset,
                    fd.declaredLength
                )
            }
        }
    }
}
