package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.repository.DocumentSessionRepository

class SaveSignatureUseCase(
    private val documentSessionRepository: DocumentSessionRepository,
) {
    operator fun invoke(signatureBytes: ByteArray) {
        documentSessionRepository.updateSignature(signatureBytes)
    }
}
