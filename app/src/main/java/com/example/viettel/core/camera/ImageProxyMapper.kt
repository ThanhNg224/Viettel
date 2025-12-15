package com.example.viettel.core.camera

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

object ImageProxyMapper {

    @OptIn(ExperimentalGetImage::class)
    fun toJpegBytes(imageProxy: ImageProxy, quality: Int = 92): ByteArray? {
        val image = imageProxy.image ?: return null
        val nv21 = yuv420ToNv21(imageProxy)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), quality, out)
        return out.toByteArray()
    }

    @ExperimentalGetImage
    fun toNv21Frame(imageProxy: ImageProxy): ImageFrame? {
        val image = imageProxy.image ?: return null
        val nv21 = yuv420ToNv21(imageProxy)
        return ImageFrame(
            data = nv21,
            width = image.width,
            height = image.height,
            rotation = imageProxy.imageInfo.rotationDegrees
        )
    }

    private fun yuv420ToNv21(image: ImageProxy): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        return nv21
    }
}
