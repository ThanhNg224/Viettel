package com.example.viettel.feature.identity.presentation.mapper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix

object BitmapMapper {
    fun fromBytes(bytes: ByteArray?, rotation: Int = 0): Bitmap? {
        if (bytes == null) return null
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        if (rotation == 0) return bitmap

        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
