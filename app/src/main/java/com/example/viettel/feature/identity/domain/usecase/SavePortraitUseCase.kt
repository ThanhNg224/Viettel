package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.entity.PortraitAction
import com.example.viettel.feature.identity.domain.repository.DocumentSessionRepository
import javax.inject.Inject

class SavePortraitUseCase @Inject constructor(
    private val documentSessionRepository: DocumentSessionRepository,
) {
    operator fun invoke(action: PortraitAction, imageBytes: ByteArray) {
        documentSessionRepository.updatePortrait(action, imageBytes)
    }
}
