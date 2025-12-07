package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.entity.PortraitAction
import com.example.viettel.feature.identity.domain.repository.DocumentSessionRepository

class SavePortraitUseCase(
    private val documentSessionRepository: DocumentSessionRepository,
) {
    operator fun invoke(action: PortraitAction, imageBytes: ByteArray) {
        documentSessionRepository.updatePortrait(action, imageBytes)
    }
}
