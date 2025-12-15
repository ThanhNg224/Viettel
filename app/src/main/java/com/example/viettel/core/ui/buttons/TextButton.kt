package com.example.viettel.core.ui.buttons

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import com.example.viettel.R
import com.example.viettel.core.extensions.getColorList
import com.example.viettel.core.extensions.spToPx
import com.example.viettel.core.utils.ShapeUtils
import com.example.viettel.listener.VoidCallback
import kotlin.math.abs
import kotlin.math.roundToInt

class TextButton : AppCompatTextView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs)
    }

    private fun initView(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextButton)
        try {
            val buttonBackgroundSkip = typedArray.getBoolean(R.styleable.TextButton_button_background_skip, false)
            if (buttonBackgroundSkip) return

            // ---- Background (solid + gradient) ----
            val colorBackground = typedArray.getColor(R.styleable.TextButton_button_color, Color.TRANSPARENT)
            val gradientBackgroundColors = typedArray.getColorList(context, R.styleable.TextButton_button_backgroundColors)

            // ---- Border (solid + gradient) ----
            val borderColor =
                typedArray.getColor(R.styleable.TextButton_button_borderColor, Color.TRANSPARENT)
            val gradientBorderColors = typedArray.getColorList(context, R.styleable.TextButton_button_borderColors)


            val borderWidth = typedArray.getDimensionPixelSize(R.styleable.TextButton_button_borderWidth, 0)

            val orientationIndex =
                typedArray.getInt(R.styleable.TextButton_button_orientation, 0)
            val orientation = ShapeUtils.Companion.GradientOrientation.entries.toTypedArray()
                .getOrElse(orientationIndex) { ShapeUtils.Companion.GradientOrientation.LEFT_RIGHT }

            val shape = typedArray.getInt(R.styleable.TextButton_button_shape, 0)
            if (shape == 1) {
                // Oval
                background = ShapeUtils.oval(
                    context,
                    backgroundColor = colorBackground,
                    backgroundColors = gradientBackgroundColors,
                    borderColor = borderColor,
                    borderColors = gradientBorderColors,
                    borderWidth = borderWidth.toFloat(),
                    orientation = orientation
                )
            } else {
                // Rectangle
                val radius =
                    typedArray.getDimensionPixelSize(R.styleable.TextButton_button_radius, 0)
                val radiusTopLeft =
                    typedArray.getDimensionPixelSize(R.styleable.TextButton_button_radiusTopLeft, 0)
                val radiusTopRight =
                    typedArray.getDimensionPixelSize(R.styleable.TextButton_button_radiusTopRight, 0)
                val radiusBottomLeft =
                    typedArray.getDimensionPixelSize(
                        R.styleable.TextButton_button_radiusBottomLeft,
                        0
                    )
                val radiusBottomRight =
                    typedArray.getDimensionPixelSize(
                        R.styleable.TextButton_button_radiusBottomRight,
                        0
                    )

                background = ShapeUtils.rectangle(
                    context,
                    backgroundColor = colorBackground,
                    backgroundColors = gradientBackgroundColors,
                    borderColor = borderColor,
                    borderColors = gradientBorderColors,
                    borderWidth = borderWidth.toFloat(),
                    radiusTopLeft = radiusTopLeft.toFloat(),
                    radiusTopRight = radiusTopRight.toFloat(),
                    radiusBottomRight = radiusBottomRight.toFloat(),
                    radiusBottomLeft = radiusBottomLeft.toFloat(),
                    radius = radius.toFloat(),
                    orientation = orientation
                )
            }

            val isClipToOutline = typedArray.getBoolean(R.styleable.TextButton_button_clipToOutline, false)
            setClipToOutline(isClipToOutline)


            val isUnderline = typedArray.getBoolean(R.styleable.TextButton_button_underline, false)
            if (isUnderline) {
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }

            val format = typedArray.getString(R.styleable.TextButton_button_format)
            val rawText = text?.toString() ?: ""
            if (!format.isNullOrEmpty()) {
                text = String.format(format, rawText)
            }

            val autoSizeText = typedArray.getBoolean(R.styleable.TextButton_button_autoSizeText, false)
            if (autoSizeText) {
                // === AutoSize: min = 1sp, max = textSize hiện tại, step = 1sp ===
                val minPx = 1f.spToPx().toInt()
                val stepPx = 1f.spToPx().toInt()
                var maxPx: Int = textSize.roundToInt() // textSize hiện tại (px)
                if (maxPx < minPx) maxPx = minPx

                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    this, minPx, maxPx, stepPx, TypedValue.COMPLEX_UNIT_PX
                )
            }

        } finally {
            // Đảm bảo giải phóng tài nguyên
            typedArray.recycle()
        }
    }

    private val clickDelay: Long = 500 // 0.5 giây
    private var lastClickTime: Long = 0


    fun onClickListener(callback: VoidCallback?) {
        setOnClickListener(
            OnClickListener { _ ->
                val currentTime = System.currentTimeMillis()
                if (abs((currentTime - lastClickTime).toDouble()) < clickDelay) return@OnClickListener
                lastClickTime = currentTime
                callback?.execute()
            },
        )
    }

}
