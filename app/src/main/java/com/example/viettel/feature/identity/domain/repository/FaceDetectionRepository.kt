package com.example.viettel.feature.identity.domain.repository

import com.example.viettel.feature.identity.domain.entity.FaceAttributes
import com.example.viettel.feature.identity.domain.entity.ImageFrame

interface FaceDetectionRepository {
    suspend fun detectFace(frame: ImageFrame): Result<FaceAttributes>
}
