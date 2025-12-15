package com.example.viettel.core.extensions

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.StyleableRes

fun TypedArray.getColorList(context: Context, @StyleableRes index: Int): List<Int>? {
    val resId = getResourceId(index, 0)
    if (resId == 0) return null

    val ta = context.resources.obtainTypedArray(resId)
    return try {
        buildList(ta.length()) { for (i in 0 until ta.length()) add(ta.getColor(i, 0)) }
    } finally {
        ta.recycle()
    }
}

