package com.denzygames.mobilepos

import android.graphics.*
import android.graphics.drawable.Drawable

class CodeDrawable (private val model: CodeViewModel) : Drawable(){

    private val boundingRectPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 3F
        alpha = 150
    }

    private val contentRectPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.RED
        alpha = 255
    }

    private val contentTextPaint = Paint().apply {
        color = Color.YELLOW
        alpha = 255
        textSize = 36F
    }
    private val contentPadding = 25
    private var textWidth = contentTextPaint.measureText(model.content).toInt()

    override fun draw(canvas: Canvas) {
        canvas.drawRect(model.boundingRect, boundingRectPaint)
        canvas.drawRect(
            Rect(
                model.boundingRect.left,
                model.boundingRect.bottom + contentPadding/2,
                model.boundingRect.left + textWidth + contentPadding*2,
                model.boundingRect.bottom + contentTextPaint.textSize.toInt() + contentPadding),
            contentRectPaint
        )
        canvas.drawText(
            model.content,
            (model.boundingRect.left + contentPadding).toFloat(),
            (model.boundingRect.bottom + contentPadding*2).toFloat(),
            contentTextPaint
        )
    }

    override fun setAlpha(alpha: Int) {
        boundingRectPaint.alpha = alpha
        contentRectPaint.alpha = alpha
        contentTextPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        boundingRectPaint.colorFilter = colorFilter
        contentRectPaint.colorFilter = colorFilter
        contentTextPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}