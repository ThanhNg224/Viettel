package com.example.viettel.feature.identity.integration.ocr

import android.graphics.BitmapFactory
import com.example.viettel.feature.identity.domain.entity.CapturedImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jmrtd.lds.icao.MRZInfo
import vn.leeon.eidsdk.utils.OcrUtils
import kotlin.coroutines.resume

class OcrMrzDataSource {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractMrz(capturedImage: CapturedImage): Result<MRZInfo> =
        suspendCancellableCoroutine { continuation ->
            val bitmap = BitmapFactory.decodeByteArray(capturedImage.data, 0, capturedImage.data.size)
            val inputImage = InputImage.fromBitmap(bitmap, capturedImage.rotationDegrees)
            val startTime = System.currentTimeMillis()

            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val elapsed = System.currentTimeMillis() - startTime
                    OcrUtils.processOcr(visionText, elapsed, object : OcrUtils.MRZCallback {
                        override fun onMRZRead(mrz: MRZInfo, timeRequired: Long) {
                            if (continuation.isActive) continuation.resume(Result.success(mrz))
                        }

                        override fun onMRZReadFailure(timeRequired: Long) {
                            if (continuation.isActive) continuation.resume(Result.failure(IllegalStateException("MRZ not found")))
                        }

                        override fun onFailure(e: Exception, timeRequired: Long) {
                            if (continuation.isActive) continuation.resume(Result.failure(e))
                        }
                    })
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        }
}
