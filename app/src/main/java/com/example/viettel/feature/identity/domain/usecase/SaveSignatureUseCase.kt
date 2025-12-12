package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.repository.DocumentSessionRepository
import javax.inject.Inject

class SaveSignatureUseCase @Inject constructor(
    private val documentSessionRepository: DocumentSessionRepository,
) {
    operator fun invoke(signatureBytes: ByteArray) {
        documentSessionRepository.updateSignature(signatureBytes)
    }
}
