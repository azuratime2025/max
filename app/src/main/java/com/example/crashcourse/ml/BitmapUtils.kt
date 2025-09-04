package com.example.crashcourse.ml

import android.graphics.*
import android.media.Image
import androidx.core.graphics.scale
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.ByteArrayOutputStream

object BitmapUtils {
    private const val INPUT_SIZE = 160  // or your FaceNet input
    private const val BYTES_PER_CHANNEL = 4

    /** Convert YUV Image → RGB Bitmap → crop→rotate→resize→FloatBuffer */
    fun preprocessFace(image: Image, boundingBox: Rect, rotation: Int): ByteBuffer {
        // 1) Convert to Bitmap (YUV→RGB)
        val bitmap = yuvToRgb(image)

        // 2) Rotate if needed
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // 3) Crop the face
        val left   = boundingBox.left.coerceAtLeast(0)
        val top    = boundingBox.top.coerceAtLeast(0)
        val width  = boundingBox.width().coerceAtMost(rotated.width  - left)
        val height = boundingBox.height().coerceAtMost(rotated.height - top)
        val faceBmp = Bitmap.createBitmap(rotated, left, top, width, height)

        // 4) Resize to model input
        val inputBmp = faceBmp.scale(INPUT_SIZE, INPUT_SIZE)

        // 5) Normalize pixels to [-1,1] and pack into ByteBuffer
        val buffer = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * BYTES_PER_CHANNEL)
            .order(ByteOrder.nativeOrder())
        val intVals = IntArray(INPUT_SIZE * INPUT_SIZE)
        inputBmp.getPixels(intVals, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        var idx = 0
        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val pixel = intVals[idx++]
                buffer.putFloat(((pixel shr 16 and 0xFF) - 127.5f) / 128f)
                buffer.putFloat(((pixel shr 8  and 0xFF) - 127.5f) / 128f)
                buffer.putFloat(((pixel       and 0xFF) - 127.5f) / 128f)
            }
        }
        buffer.rewind()
        return buffer
    }

    /** Extract face bitmap from image with rotation and cropping */
    fun extractFaceBitmap(image: Image, boundingBox: Rect, rotation: Int): Bitmap {
        // 1) Convert YUV image to RGB bitmap
        val bitmap = yuvToRgb(image)

        // 2) Rotate the image by the given angle
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // 3) Crop the face area using the bounding box
        val left   = boundingBox.left.coerceAtLeast(0)
        val top    = boundingBox.top.coerceAtLeast(0)
        val width  = boundingBox.width().coerceAtMost(rotated.width - left)
        val height = boundingBox.height().coerceAtMost(rotated.height - top)

        // 4) Return the cropped face bitmap
        return Bitmap.createBitmap(rotated, left, top, width, height)
    }

    /** Simple YUV to RGB conversion via ScriptIntrinsicYuvToRGB */
    fun yuvToRgb(image: Image): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val jpg = out.toByteArray()
        return BitmapFactory.decodeByteArray(jpg, 0, jpg.size)
    }
}
