package com.eunho.crossfitposedetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class Display @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var srcRect = Rect(0, 0, 480, 640)
    private var disRect: Rect? = null
    private var b: Bitmap? = null

    fun getBitmap(bitmap: Bitmap) {
        b = bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        invalidate()
        disRect = Rect(0, 0, right, bottom)
        if (b != null) {
            b?.let { canvas.drawBitmap(it, srcRect, disRect!!, null) }
        }
    }
}