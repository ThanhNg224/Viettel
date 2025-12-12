package com.example.viettel.feature.identity.data.repository

import com.example.viettel.feature.identity.domain.repository.FaceMatchRepository
import com.example.viettel.feature.identity.integration.facematch.FaceMatchRemoteDataSource
import javax.inject.Inject

class FaceMatchRepositoryImpl @Inject constructor(
    private val remoteDataSource: FaceMatchRemoteDataSource,
) : FaceMatchRepository {
    override suspend fun comparePortraits(smilePortrait: ByteArray, chipPortrait: ByteArray): Result<Double> {
        return remoteDataSource.compare(smilePortrait, chipPortrait)
    }
}
