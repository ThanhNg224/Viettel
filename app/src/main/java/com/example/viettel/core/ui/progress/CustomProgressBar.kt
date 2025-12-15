package com.example.viettel.core.ui.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.viettel.R

class CustomProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var progress: Float = 0f
    private var backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private var progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CustomProgressBar)
        val bgColor = ta.getColor(
            R.styleable.CustomProgressBar_backgroundColor,
            ContextCompat.getColor(context, R.color.grey)
        )
        val prColor = ta.getColor(
            R.styleable.CustomProgressBar_progressColor,
            ContextCompat.getColor(context, R.color.green)
        )
        progress = ta.getFloat(R.styleable.CustomProgressBar_progressValue, 0f)
        ta.recycle()

        backgroundPaint.color = bgColor
        progressPaint.color = prColor
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
        invalidate()
    }

    fun setProgressColor(color: Int) {
        progressPaint.color = color
        invalidate()
    }

    fun setBackgroundColorCustom(color: Int) {
        backgroundPaint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = height / 2f
        val backgroundRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val progressRect = RectF(0f, 0f, width * progress, height.toFloat())

        // Vẽ background
        canvas.drawRoundRect(backgroundRect, radius, radius, backgroundPaint)
        // Vẽ progress
        canvas.drawRoundRect(progressRect, radius, radius, progressPaint)
    }
}
