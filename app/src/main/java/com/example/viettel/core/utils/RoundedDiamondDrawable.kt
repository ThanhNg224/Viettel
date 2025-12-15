package com.example.viettel.core.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import kotlin.math.min
import androidx.core.graphics.withTranslation
import kotlin.math.sqrt

class RoundedDiamondDrawable(
    bgColor: Int,
    bdColor: Int? = null,
    bdWidth: Float = 0f,
    private val cornerRadius: Float = 0f
) : Drawable() {

    private val paintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = bgColor
        isDither = true
    }
    private val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = bdWidth
        color = bdColor ?: Color.TRANSPARENT
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isDither = true
    }

    override fun draw(canvas: Canvas) {
        val w = bounds.width().toFloat()
        val h = bounds.height().toFloat()
        if (w <= 0f || h <= 0f) return

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()

        // Chọn cạnh của hình vuông sẽ được xoay 45° để KHÔNG bị cắt bởi bounds.
        // Bởi vì square xoay 45° có bbox = side * √2, nên side = (minDim - stroke) / √2
        val minDim = min(w, h)
        val side = ((minDim - paintStroke.strokeWidth) / SQRT2).coerceAtLeast(0f)

        val half = side / 2f
        val rect = RectF(-half, -half, half, half)

        // Bo góc tối đa = half (round-rect chuẩn)
        val r = cornerRadius.coerceAtMost(half)

        canvas.withTranslation(cx, cy) {
            rotate(45f)

            // Fill trước
            drawRoundRect(rect, r, r, paintFill)

            // Stroke sau (nếu có)
            if (paintStroke.color != Color.TRANSPARENT && paintStroke.strokeWidth > 0f) {
                drawRoundRect(rect, r, r, paintStroke)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        paintFill.alpha = alpha
        paintStroke.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paintFill.colorFilter = colorFilter
        paintStroke.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    companion object {
        private val SQRT2 = sqrt(2f)
    }
}