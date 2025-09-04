package com.example.crashcourse.ui

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.min

@Composable
fun FaceOverlay(
    faceBounds: List<Rect>,
    imageSize: IntSize,
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier,
    paddingFactor: Float = 0f
) {
    if (faceBounds.isEmpty() || imageSize.width == 0) return

    Canvas(modifier) {
        val scaleX = size.width / imageSize.width.toFloat()
        val scaleY = size.height / imageSize.height.toFloat()
        val scale = min(scaleX, scaleY)
        val offX = (size.width - imageSize.width * scale) / 2f
        val offY = (size.height - imageSize.height * scale) / 2f

        faceBounds.forEach { r ->
            val side = max(r.width(), r.height()) * (1 + paddingFactor)
            val cx = (r.left + r.right) / 2f
            val cy = (r.top + r.bottom) / 2f

            val leftInImage = if (isFrontCamera)
                imageSize.width - (cx + side / 2f)
            else
                cx - side / 2f
            val topInImage = cy - side / 2f

            drawRect(
                color = Color.Green,
                topLeft = Offset(
                    x = leftInImage * scale + offX,
                    y = topInImage * scale + offY
                ),
                size = Size(side * scale, side * scale),
                style = Stroke(width = 4f)
            )
        }
    }
}