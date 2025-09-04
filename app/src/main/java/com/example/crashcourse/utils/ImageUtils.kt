package com.example.crashcourse.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer
    
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    
    val nv21 = ByteArray(ySize + uSize + vSize)
    
    // Copy Y, U, V planes
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)
    
    val yuvImage = YuvImage(
        nv21, ImageFormat.NV21, 
        this.width, this.height, null
    )
    
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(
        Rect(0, 0, yuvImage.width, yuvImage.height), 90, out
    )
    
    val imageBytes = out.toByteArray()
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    
    // Rotate if needed
    val matrix = Matrix()
    matrix.postRotate(this.imageInfo.rotationDegrees.toFloat())
    
    return Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
    )
}