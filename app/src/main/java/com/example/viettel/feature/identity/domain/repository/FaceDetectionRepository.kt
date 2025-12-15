package com.example.viettel.feature.identity.domain.repository

import com.example.viettel.core.camera.ImageFrame
import com.example.viettel.feature.identity.domain.entity.FaceAttributes

interface FaceDetectionRepository {
    suspend fun detectFace(frame: ImageFrame): Result<FaceAttributes>
}
