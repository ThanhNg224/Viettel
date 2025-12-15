package com.example.viettel.core.ui.buttons

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.example.viettel.R
import com.example.viettel.core.extensions.getColorList
import com.example.viettel.core.utils.ShapeUtils
import com.example.viettel.listener.VoidCallback
import kotlin.math.abs

class ViewButton : View {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs)
    }

    private fun initView(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ViewButton)
        try {
            val buttonBackgroundSkip = typedArray.getBoolean(R.styleable.ViewButton_button_background_skip, false)
            if (buttonBackgroundSkip) return

            // ---- Background (solid + gradient) ----
            val colorBackground = typedArray.getColor(R.styleable.ViewButton_button_color, Color.TRANSPARENT)
            val gradientBackgroundColors = typedArray.getColorList(context, R.styleable.ViewButton_button_backgroundColors)

            // ---- Border (solid + gradient) ----
            val borderColor =
                typedArray.getColor(R.styleable.ViewButton_button_borderColor, Color.TRANSPARENT)
            val gradientBorderColors = typedArray.getColorList(context, R.styleable.ViewButton_button_borderColors)

            val borderWidth = typedArray.getDimensionPixelSize(R.styleable.ViewButton_button_borderWidth, 0)

            val orientationIndex =
                typedArray.getInt(R.styleable.ViewButton_button_orientation, 0)
            val orientation = ShapeUtils.Companion.GradientOrientation.entries.toTypedArray()
                .getOrElse(orientationIndex) { ShapeUtils.Companion.GradientOrientation.LEFT_RIGHT }

            val shape = typedArray.getInt(R.styleable.ViewButton_button_shape, 0)
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
                    typedArray.getDimensionPixelSize(R.styleable.ViewButton_button_radius, 0)
                val radiusTopLeft =
                    typedArray.getDimensionPixelSize(R.styleable.ViewButton_button_radiusTopLeft, 0)
                val radiusTopRight =
                    typedArray.getDimensionPixelSize(R.styleable.ViewButton_button_radiusTopRight, 0)
                val radiusBottomLeft =
                    typedArray.getDimensionPixelSize(
                        R.styleable.ViewButton_button_radiusBottomLeft,
                        0
                    )
                val radiusBottomRight =
                    typedArray.getDimensionPixelSize(
                        R.styleable.ViewButton_button_radiusBottomRight,
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

            val isClipToOutline = typedArray.getBoolean(R.styleable.ViewButton_button_clipToOutline, false)
            setClipToOutline(isClipToOutline)
        } finally {
            // Đảm bảo giải phóng tài nguyên
            typedArray.recycle()
        }
    }

    private val clickDelay: Long = 500 // 0.5 giây
    private var lastClickTime: Long = 0


    fun onClickListener(callback: VoidCallback?) {
        setOnClickListener(
            OnClickListener { view: View? ->
                val currentTime = System.currentTimeMillis()
                if (abs((currentTime - lastClickTime).toDouble()) < clickDelay) return@OnClickListener
                lastClickTime = currentTime
                callback?.execute()
            },
        )
    }

}
