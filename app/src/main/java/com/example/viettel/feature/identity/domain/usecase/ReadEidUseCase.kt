package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.entity.EidData
import com.example.viettel.feature.identity.domain.repository.DocumentSessionRepository
import com.example.viettel.feature.identity.domain.repository.EidRepository

class ReadEidUseCase(
    private val eidRepository: EidRepository,
    private val documentSessionRepository: DocumentSessionRepository,
) {
    suspend operator fun invoke(): Result<EidData> {
        val session = documentSessionRepository.getSession()
        val mrzData = session.mrzData ?: return Result.failure(IllegalStateException("MRZ data not ready"))

        val eidResult = eidRepository.readEid(mrzData)
        eidResult.getOrNull()?.let { documentSessionRepository.updateEidData(it) }
        return eidResult
    }
}
