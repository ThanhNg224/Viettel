package com.example.viettel.core.ui.toast

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import com.example.viettel.R
import com.example.viettel.core.extensions.dpToPx
import com.example.viettel.core.extensions.pxToSp
import com.example.viettel.core.utils.ShapeUtils

enum class ToastType(val bgColor: Int, val textColor: Int) {
    SUCCESS("#78ab4f".toColorInt(), Color.WHITE),
    ERROR("#DC4E41".toColorInt(), Color.WHITE),
    WARNING("#FABE0E".toColorInt(), Color.WHITE),
    INFO("#706D6D".toColorInt(), Color.WHITE)
}

@SuppressLint("InflateParams")
fun showCustomToast(
    context: Context,
    message: String,
    type: ToastType,
    textSize: Float,
    font: Typeface?,
    gravity: Int,
    yOffset: Int
) {
    val layout = LayoutInflater.from(context).inflate(R.layout.toast_custom, null)
    layout.findViewById<TextView>(R.id.toast_text).apply {
        text = message
        setTextColor(type.textColor)
        this.textSize = textSize.pxToSp()
        font?.let { typeface = it }
    }
    layout.findViewById<LinearLayout>(R.id.toast_container).background = ShapeUtils.rectangle(
        context = context,
        backgroundColor = type.bgColor,
        radius = 30f.dpToPx(),
        borderColor = Color.TRANSPARENT,
        borderWidth = 0f,
    )

    Toast(context).apply {
        duration = Toast.LENGTH_SHORT
        view = layout
        setGravity(gravity, 0, yOffset)
        show()
    }
}
