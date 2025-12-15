package com.example.viettel.feature.identity.integration.face

import com.example.viettel.core.camera.ImageFrame
import com.example.viettel.feature.identity.domain.entity.FaceAttributes
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class FaceDetectionDataSource @Inject constructor() {

    private val detector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build()
        FaceDetection.getClient(options)
    }

    suspend fun detect(frame: ImageFrame): Result<FaceAttributes> =
        suspendCancellableCoroutine { continuation ->
            val inputImage = InputImage.fromByteArray(
                frame.data,
                frame.width,
                frame.height,
                frame.rotation,
                InputImage.IMAGE_FORMAT_NV21
            )

            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    val face = faces.firstOrNull()
                    if (face == null) {
                        if (continuation.isActive) continuation.resume(
                            Result.failure(IllegalStateException("Khong thay khuon mat"))
                        )
                    } else {
                        val attrs = FaceAttributes(
                            smilingProbability = face.smilingProbability ?: -1f,
                            leftEyeOpenProbability = face.leftEyeOpenProbability ?: -1f,
                            rightEyeOpenProbability = face.rightEyeOpenProbability ?: -1f,
                            headYaw = face.headEulerAngleY,
                        )
                        if (continuation.isActive) continuation.resume(Result.success(attrs))
                    }
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resume(Result.failure(e))
                }
        }
}
