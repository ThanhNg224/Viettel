package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.repository.FaceMatchRepository

class ComparePortraitUseCase(
    private val faceMatchRepository: FaceMatchRepository,
) {
    suspend operator fun invoke(smilePortrait: ByteArray, chipPortrait: ByteArray): Result<Double> {
        return faceMatchRepository.comparePortraits(smilePortrait, chipPortrait)
    }
}
