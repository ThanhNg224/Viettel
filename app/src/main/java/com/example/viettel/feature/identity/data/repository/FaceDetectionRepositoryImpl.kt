package com.example.viettel.feature.identity.data.repository

import com.example.viettel.feature.identity.domain.entity.FaceAttributes
import com.example.viettel.feature.identity.domain.entity.ImageFrame
import com.example.viettel.feature.identity.domain.repository.FaceDetectionRepository
import com.example.viettel.feature.identity.integration.face.FaceDetectionDataSource

class FaceDetectionRepositoryImpl(
    private val dataSource: FaceDetectionDataSource,
) : FaceDetectionRepository {
    override suspend fun detectFace(frame: ImageFrame): Result<FaceAttributes> {
        return dataSource.detect(frame)
    }
}
