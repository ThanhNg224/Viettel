package com.example.viettel.feature.identity.data.repository

import com.example.viettel.feature.identity.data.mapper.MrzMapper
import com.example.viettel.feature.identity.domain.entity.CapturedImage
import com.example.viettel.feature.identity.domain.entity.MrzData
import com.example.viettel.feature.identity.domain.repository.MrzRepository
import com.example.viettel.feature.identity.integration.ocr.OcrMrzDataSource

class MrzRepositoryImpl(
    private val ocrMrzDataSource: OcrMrzDataSource,
    private val mrzMapper: MrzMapper = MrzMapper(),
) : MrzRepository {

    override suspend fun extractMrz(capturedImage: CapturedImage): Result<MrzData> {
        return ocrMrzDataSource.extractMrz(capturedImage).map(mrzMapper::fromSdk)
    }
}
