package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.repository.FaceMatchRepository
import javax.inject.Inject

class ComparePortraitUseCase @Inject constructor(
    private val faceMatchRepository: FaceMatchRepository,
) {
    suspend operator fun invoke(smilePortrait: ByteArray, chipPortrait: ByteArray): Result<Double> {
        return faceMatchRepository.comparePortraits(smilePortrait, chipPortrait)
    }
}
