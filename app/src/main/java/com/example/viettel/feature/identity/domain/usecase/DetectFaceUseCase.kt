package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.core.camera.ImageFrame
import com.example.viettel.feature.identity.domain.entity.FaceAttributes
import com.example.viettel.feature.identity.domain.repository.FaceDetectionRepository
import javax.inject.Inject

class DetectFaceUseCase @Inject constructor(
    private val repository: FaceDetectionRepository,
) {
    suspend operator fun invoke(frame: ImageFrame): Result<FaceAttributes> {
        return repository.detectFace(frame)
    }
}
