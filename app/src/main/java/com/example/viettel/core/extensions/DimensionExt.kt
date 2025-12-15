package com.example.viettel.core.extensions

import android.content.res.Resources
import kotlin.math.roundToInt

fun Float.spToPx(): Float = this * Resources.getSystem().displayMetrics.scaledDensity

fun Float.pxToSp(): Float = this / Resources.getSystem().displayMetrics.scaledDensity

fun Float.dpToPx(): Float = this * Resources.getSystem().displayMetrics.density

fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).roundToInt()

