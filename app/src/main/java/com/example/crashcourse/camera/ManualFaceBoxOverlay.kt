package com.example.crashcourse.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

/**
 * ManualFaceBoxOverlay - Independent face box overlay for manual capture
 * This overlay is completely separate from the main attendance system
 */
class ManualFaceBoxOverlay(context: Context) : View(context) {

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.GREEN
        strokeWidth = 8f
        isAntiAlias = true
    }

    var faceBounds: List<Rect> = listOf()
        set(value) {
            field = value
            invalidate() // Trigger redraw
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw green rectangles around detected faces
        faceBounds.forEach { rect ->
            canvas.drawRect(rect, paint)
        }
    }
}
