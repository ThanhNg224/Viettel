package com.example.viettel.feature.identity.data.repository

import com.example.viettel.feature.identity.data.mapper.EidMapper
import com.example.viettel.feature.identity.data.mapper.MrzMapper
import com.example.viettel.feature.identity.domain.entity.EidData
import com.example.viettel.feature.identity.domain.entity.MrzData
import com.example.viettel.feature.identity.domain.repository.EidRepository
import com.example.viettel.feature.identity.integration.eid.EidReaderDataSource
import javax.inject.Inject

class EidRepositoryImpl @Inject constructor(
    private val eidReaderDataSource: EidReaderDataSource,
    private val mrzMapper: MrzMapper,
    private val eidMapper: EidMapper,
) : EidRepository {

    override suspend fun readEid(mrzData: MrzData): Result<EidData> {
        val sdkMrz = mrzMapper.toSdk(mrzData)
        return eidReaderDataSource.readEid(sdkMrz).map(eidMapper::fromSdk)
    }
}
