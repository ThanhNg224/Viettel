package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.entity.CapturedImage
import com.example.viettel.feature.identity.domain.entity.MrzData
import com.example.viettel.feature.identity.domain.repository.DocumentSessionRepository
import com.example.viettel.feature.identity.domain.repository.MrzRepository
import javax.inject.Inject

class ExtractMrzUseCase @Inject constructor(
    private val mrzRepository: MrzRepository,
    private val documentSessionRepository: DocumentSessionRepository,
) {
    suspend operator fun invoke(capturedImage: CapturedImage): Result<MrzData> {
        val mrzResult = mrzRepository.extractMrz(capturedImage)
        mrzResult.getOrNull()?.let { documentSessionRepository.updateMrzData(it) }
        return mrzResult
    }
}
