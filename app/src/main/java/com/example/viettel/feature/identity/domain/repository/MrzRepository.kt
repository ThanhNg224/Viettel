package com.example.viettel.feature.identity.domain.repository

import com.example.viettel.feature.identity.domain.entity.CapturedImage
import com.example.viettel.feature.identity.domain.entity.MrzData

interface MrzRepository {
    suspend fun extractMrz(capturedImage: CapturedImage): Result<MrzData>
}
