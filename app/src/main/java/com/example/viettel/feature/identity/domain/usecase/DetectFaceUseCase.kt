package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.entity.FaceAttributes
import com.example.viettel.feature.identity.domain.entity.ImageFrame
import com.example.viettel.feature.identity.domain.repository.FaceDetectionRepository

class DetectFaceUseCase(
    private val repository: FaceDetectionRepository,
) {
    suspend operator fun invoke(frame: ImageFrame): Result<FaceAttributes> {
        return repository.detectFace(frame)
    }
}
