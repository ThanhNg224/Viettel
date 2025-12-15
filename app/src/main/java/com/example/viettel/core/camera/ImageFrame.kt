package com.example.viettel.core.camera

data class ImageFrame(
    val data: ByteArray,
    val width: Int,
    val height: Int,
    val rotation: Int,
)

