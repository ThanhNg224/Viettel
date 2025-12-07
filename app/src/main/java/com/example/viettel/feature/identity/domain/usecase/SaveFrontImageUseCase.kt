package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.repository.DocumentSessionRepository

class SaveFrontImageUseCase(
    private val documentSessionRepository: DocumentSessionRepository,
) {
    operator fun invoke(imageBytes: ByteArray, rotation: Int) {
        documentSessionRepository.updateFrontImage(imageBytes, rotation)
    }
}
