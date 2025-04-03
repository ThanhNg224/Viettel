package com.example.viettel.utils

import android.graphics.*
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

fun ImageProxy.toBitmapSafe(): Bitmap? {
    val planeProxy = planes.firstOrNull() ?: return null
    val buffer = planeProxy.buffer
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    val yuvImage = YuvImage(bytes, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
    val byteArray = out.toByteArray()
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}
