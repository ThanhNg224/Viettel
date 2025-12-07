package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.entity.DocumentType
import com.example.viettel.feature.identity.domain.repository.DocumentSessionRepository

class SaveDocumentTypeUseCase(
    private val documentSessionRepository: DocumentSessionRepository,
) {
    operator fun invoke(documentType: DocumentType) {
        documentSessionRepository.updateDocumentType(documentType)
    }
}
