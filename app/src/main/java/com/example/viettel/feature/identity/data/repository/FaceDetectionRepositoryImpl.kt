package com.example.viettel.feature.identity.data.repository

import com.example.viettel.core.camera.ImageFrame
import com.example.viettel.feature.identity.domain.entity.FaceAttributes
import com.example.viettel.feature.identity.domain.repository.FaceDetectionRepository
import com.example.viettel.feature.identity.integration.face.FaceDetectionDataSource
import javax.inject.Inject

class FaceDetectionRepositoryImpl @Inject constructor(
    private val dataSource: FaceDetectionDataSource,
) : FaceDetectionRepository {
    override suspend fun detectFace(frame: ImageFrame): Result<FaceAttributes> {
        return dataSource.detect(frame)
    }
}
