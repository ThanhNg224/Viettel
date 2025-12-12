package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.repository.DocumentSessionRepository
import javax.inject.Inject

class SaveBackImageUseCase @Inject constructor(
    private val documentSessionRepository: DocumentSessionRepository,
) {
    operator fun invoke(imageBytes: ByteArray, rotation: Int) {
        documentSessionRepository.updateBackImage(imageBytes, rotation)
    }
}
