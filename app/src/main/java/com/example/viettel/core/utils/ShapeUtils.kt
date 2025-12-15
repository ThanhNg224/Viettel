package com.example.viettel.core.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt

class ShapeUtils {
    companion object {
        private const val RECTANGLE: String = "rectangle"
        private const val OVAL: String = "oval"
        private const val DIAMOND: String = "diamond"

        enum class GradientOrientation(val orientation: GradientDrawable.Orientation) {
            LEFT_RIGHT(GradientDrawable.Orientation.LEFT_RIGHT),
            TOP_BOTTOM(GradientDrawable.Orientation.TOP_BOTTOM),
            TL_BR(GradientDrawable.Orientation.TL_BR),
            TR_BL(GradientDrawable.Orientation.TR_BL),
            BL_TR(GradientDrawable.Orientation.BL_TR),
            BR_TL(GradientDrawable.Orientation.BR_TL)
        }

        // Phương thức xử lý màu từ 2 kiểu dữ liệu (ID màu, String mã màu)
        private fun getColorFromParam(context: Context?, color: Any?): Int? {
            if (color == null || color == 0 || color == Color.TRANSPARENT) return null
            try {
                return when (color) {
                    is Int -> {
                        try {
                            // Nếu là ID màu trong tài nguyên (R.color.*)
                            context?.let { ContextCompat.getColor(it, color) }
                        } catch (_: Resources.NotFoundException) {
                            // Nếu là màu int trực tiếp (Color.RED, Color.BLUE,...)
                            color
                        }
                    }
                    is String -> {
                        // Nếu là chuỗi màu, parse thành màu
                        try {
                            ensureColorHash(color).toColorInt()
                        } catch (_: IllegalArgumentException) {
                            null
                        }
                    }

                    else -> null
                }
            } catch (_: Resources.NotFoundException) {
                return null
            }
        }

        // Phương thức chung cho tất cả các loại Drawable (rectangle, oval)
        private fun init(
            shape: String?, backgroundColor: Int?, borderColor: Int?, borderWidth: Int?, radius: Float?, cornerRadii: FloatArray?
        ): GradientDrawable {
            val drawable = GradientDrawable()
            // Chọn loại shape
            if (shape == OVAL) {
                drawable.setShape(GradientDrawable.OVAL)
            } else {
                drawable.setShape(GradientDrawable.RECTANGLE)
            }

            // Đặt màu nền
            if (backgroundColor != null) {
                drawable.setColor(backgroundColor)
            }

            // Đặt màu viền và độ rộng viền
            if (borderColor != null && borderWidth != null) {
                drawable.setStroke(borderWidth, borderColor)
            }

            // Đặt bán kính góc hoặc bán kính corner cụ thể
            if (cornerRadii != null) {
                drawable.setCornerRadii(cornerRadii)
            } else if (radius != null) {
                drawable.setCornerRadius(radius)
            }

            return drawable
        }

        // Phương thức chung cho các hình rectangle hoặc oval với các tùy chọn màu sắc, viền, radius
        private fun shape(
            context: Context?, shape: String, backgroundColor: Any?, borderColor: Any?, borderWidth: Float?, radius: Float?, cornerRadii: FloatArray?
        ): GradientDrawable? {
            if (context == null) return null

            val bgColor = getColorFromParam(context, backgroundColor)
            val bdColor = getColorFromParam(context, borderColor)

            // Kiểm tra shape và khởi tạo drawable tương ứng
            if (shape == RECTANGLE) {
                return init(
                    shape = shape,
                    backgroundColor = bgColor,
                    borderColor = bdColor,
                    borderWidth = borderWidth?.toInt(),
                    radius = radius,
                    cornerRadii = cornerRadii
                )
            } else if (shape == OVAL) {
                return init(
                    shape = shape,
                    backgroundColor = bgColor,
                    borderColor = bdColor,
                    borderWidth = borderWidth?.toInt(),
                    radius = radius,
                    cornerRadii = cornerRadii
                )
            }
            return null
        }

        // Phương thức tạo hình chữ nhật
        // sử dụng cần ghi chú rõ ràng về các tham số
        fun rectangle(
            context: Context?,
            backgroundColor: Any? = null,                 // màu đơn
            backgroundColors: List<Any?>? = null,         // gradient bg
            borderColor: Any? = null,                     // màu viền đơn
            borderColors: List<Any?>? = null,             // gradient viền
            borderWidth: Float? = null,
            radius: Float? = null,
            radiusTopLeft: Float? = null,
            radiusTopRight: Float? = null,
            radiusBottomRight: Float? = null,
            radiusBottomLeft: Float? = null,
            orientation: GradientOrientation = GradientOrientation.LEFT_RIGHT,
        ): Drawable? {
            if (context == null) return null

            val cornerRadii = listOf(
                radiusTopLeft, radiusTopRight, radiusBottomRight, radiusBottomLeft
            ).map { it ?: 0f }

            val finalCornerRadii = if (cornerRadii.all { it == 0f }) null else floatArrayOf(
                cornerRadii[0], cornerRadii[0], // top-left
                cornerRadii[1], cornerRadii[1], // top-right
                cornerRadii[2], cornerRadii[2], // bottom-right
                cornerRadii[3], cornerRadii[3]  // bottom-left
            )

            // Background drawable
            val bgDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                if (!backgroundColors.isNullOrEmpty()) {
                    val colors = backgroundColors.mapNotNull { getColorFromParam(context, it) }
                    if (colors.size >= 2) {
                        setColors(colors.toIntArray())
                        setOrientation(orientation.orientation)
                    } else if (colors.size == 1) {
                        setColor(colors[0])
                    }
                } else {
                    getColorFromParam(context, backgroundColor)?.let { setColor(it) }
                }
                if (finalCornerRadii != null) setCornerRadii(finalCornerRadii)
                else if (radius != null) cornerRadius = radius
            }

            // Border gradient?
            if (!borderColors.isNullOrEmpty() && borderWidth != null && borderWidth > 0f) {
                val bdDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    val colors = borderColors.mapNotNull { getColorFromParam(context, it) }
                    if (colors.size >= 2) {
                        setColors(colors.toIntArray())
                        setOrientation(orientation.orientation)
                    } else if (colors.size == 1) {
                        setColor(colors[0])
                    }
                    if (finalCornerRadii != null) setCornerRadii(finalCornerRadii)
                    else if (radius != null) cornerRadius = radius
                }
                return LayerDrawable(arrayOf(bdDrawable, bgDrawable)).apply {
                    setLayerInset(
                        1,
                        borderWidth.toInt(),
                        borderWidth.toInt(),
                        borderWidth.toInt(),
                        borderWidth.toInt()
                    )
                }
            }

            // Border solid
            if (borderColor != null && borderWidth != null && borderWidth > 0f) {
                getColorFromParam(context, borderColor)?.let {
                    bgDrawable.setStroke(borderWidth.toInt(), it)
                }
            }

            return bgDrawable
        }

        // Phương thức tạo hình oval
        // sử dụng cần ghi chú rõ ràng về các tham số
        fun oval(
            context: Context?,
            backgroundColor: Any? = null,               // màu đơn
            backgroundColors: List<Any?>? = null,       // gradient bg
            borderColor: Any? = null,                   // màu viền đơn
            borderColors: List<Any?>? = null,           // gradient viền
            borderWidth: Float? = null,
            orientation: GradientOrientation = GradientOrientation.LEFT_RIGHT
        ): Drawable? {
            if (context == null) return null

            val bgDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                if (!backgroundColors.isNullOrEmpty()) {
                    val colors = backgroundColors.mapNotNull { getColorFromParam(context, it) }
                    if (colors.size >= 2) {
                        setColors(colors.toIntArray())
                        setOrientation(orientation.orientation)
                    } else if (colors.size == 1) {
                        setColor(colors[0])
                    }
                } else {
                    getColorFromParam(context, backgroundColor)?.let { setColor(it) }
                }
            }

            if (!borderColors.isNullOrEmpty() && borderWidth != null && borderWidth > 0f) {
                val bdDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    val colors = borderColors.mapNotNull { getColorFromParam(context, it) }
                    if (colors.size >= 2) {
                        setColors(colors.toIntArray())
                        setOrientation(orientation.orientation)
                    } else if (colors.size == 1) {
                        setColor(colors[0])
                    }
                }
                return LayerDrawable(arrayOf(bdDrawable, bgDrawable)).apply {
                    setLayerInset(
                        1,
                        borderWidth.toInt(),
                        borderWidth.toInt(),
                        borderWidth.toInt(),
                        borderWidth.toInt()
                    )
                }
            }

            if (borderColor != null && borderWidth != null && borderWidth > 0f) {
                getColorFromParam(context, borderColor)?.let {
                    bgDrawable.setStroke(borderWidth.toInt(), it)
                }
            }

            return bgDrawable
        }

        // Hàm tạo diamond (hình thoi)
        fun diamond(
            context: Context?,
            backgroundColor: Any? = null,
            borderColor: Any? = null,
            borderWidth: Float? = null,
            radius: Float? = null,
        ): Drawable? {
            if (context == null) return null

            val bgColor = getColorFromParam(context, backgroundColor) ?: Color.TRANSPARENT
            val bdColor = getColorFromParam(context, borderColor)
            val bdWidth = borderWidth ?: 0f

            return RoundedDiamondDrawable(bgColor, bdColor, bdWidth, radius ?: 0f)
        }

        // Hàm đảm bảo chuỗi màu luôn có dấu '#'
        private fun ensureColorHash(colorString: String?): String {
            var colorString = colorString
            if (colorString == null || colorString.trim { it <= ' ' }.isEmpty()) {
                return "#000000" // Default color black
            }
            colorString = colorString.trim { it <= ' ' }
            if (!colorString.startsWith("#")) {
                colorString = "#$colorString"
            }
            return colorString
        }
    }
}

