package com.example.viettel.feature.identity.domain.repository

import com.example.viettel.feature.identity.domain.entity.EidData
import com.example.viettel.feature.identity.domain.entity.MrzData

interface EidRepository {
    suspend fun readEid(mrzData: MrzData): Result<EidData>
}
