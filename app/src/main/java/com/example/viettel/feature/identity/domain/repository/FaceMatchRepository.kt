package com.example.viettel.feature.identity.domain.repository

interface FaceMatchRepository {
    suspend fun comparePortraits(smilePortrait: ByteArray, chipPortrait: ByteArray): Result<Double>
}
