package com.example.viettel.core.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

fun Context.isTablet(): Boolean {
    val mask = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
    return mask >= Configuration.SCREENLAYOUT_SIZE_LARGE
}

@ColorInt
fun Context.getThemeColor(@AttrRes attrRes: Int): Int {
    val typedValue = TypedValue()
    val resolved = theme.resolveAttribute(attrRes, typedValue, true)
    if (!resolved) return 0
    return typedValue.data
}

