package com.example.viettel.feature.identity.data.mapper

import com.example.viettel.feature.identity.domain.entity.MrzData
import net.sf.scuba.data.Gender
import org.jmrtd.lds.icao.MRZInfo
import javax.inject.Inject

class MrzMapper @Inject constructor() {

    @Suppress("DEPRECATION")
    fun fromSdk(info: MRZInfo): MrzData = MrzData(
        documentType = info.documentCode ?: "",
        issuingState = info.issuingState ?: "",
        primaryIdentifier = info.primaryIdentifier ?: "",
        secondaryIdentifier = info.secondaryIdentifier ?: "",
        documentNumber = info.documentNumber ?: "",
        nationality = info.nationality ?: "",
        dateOfBirth = info.dateOfBirth ?: "",
        gender = info.gender ?: Gender.UNKNOWN,
        dateOfExpiry = info.dateOfExpiry ?: "",
        optionalData1 = info.optionalData1 ?: "",
        optionalData2 = info.optionalData2 ?: "",
    )

    fun toSdk(data: MrzData): MRZInfo = MRZInfo.createTD1MRZInfo(
        data.documentType,
        data.issuingState,
        data.documentNumber,
        null,
        data.dateOfBirth,
        data.gender,
        data.dateOfExpiry,
        data.nationality,
        null,
        data.primaryIdentifier,
        data.secondaryIdentifier,
    )
}
