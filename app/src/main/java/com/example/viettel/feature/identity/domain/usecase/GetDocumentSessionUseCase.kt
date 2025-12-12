package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.entity.DocumentSession
import com.example.viettel.feature.identity.domain.repository.DocumentSessionRepository
import javax.inject.Inject

class GetDocumentSessionUseCase @Inject constructor(
    private val documentSessionRepository: DocumentSessionRepository,
) {
    operator fun invoke(): DocumentSession = documentSessionRepository.getSession()
}
