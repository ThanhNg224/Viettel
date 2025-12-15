package com.example.viettel.core.ui.view_group

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.graphics.toColorInt
import com.example.viettel.R

class ShadowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var shadowColor: Int = "#40000000".toColorInt()
    private var shadowAlpha: Float = 1f
    private var shadowRadius: Float = 20f
    private var cornerRadius: Float = 24f
    private var dx: Float = 0f
    private var dy: Float = 8f

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        context.theme.obtainStyledAttributes(attrs, R.styleable.ShadowLayout, 0, 0).apply {
            try {
                shadowColor = getColor(R.styleable.ShadowLayout_shadowColor, shadowColor)
                shadowAlpha = getFloat(R.styleable.ShadowLayout_shadowAlpha, shadowAlpha)
                shadowRadius = getDimension(R.styleable.ShadowLayout_shadowRadius, shadowRadius)
                cornerRadius = getDimension(R.styleable.ShadowLayout_cornerRadius, cornerRadius)
                dx = getDimension(R.styleable.ShadowLayout_dx, dx)
                dy = getDimension(R.styleable.ShadowLayout_dy, dy)
            } finally {
                recycle()
            }
        }

        val pad = shadowRadius.toInt()
        setPadding(pad, pad, pad, pad)
    }

    override fun dispatchDraw(canvas: Canvas) {
        // vẽ shadow trước
        val alphaColor = adjustAlpha(shadowColor, shadowAlpha)
        shadowPaint.setShadowLayer(shadowRadius, dx, dy, alphaColor)
        shadowPaint.color = Color.TRANSPARENT // dummy fill, không ảnh hưởng vì sẽ bị child che

        val rect = RectF(
            shadowRadius,
            shadowRadius,
            width - shadowRadius,
            height - shadowRadius
        )
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, shadowPaint)

        // sau đó vẽ child view
        super.dispatchDraw(canvas)
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt().coerceIn(0, 255)
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }
}
