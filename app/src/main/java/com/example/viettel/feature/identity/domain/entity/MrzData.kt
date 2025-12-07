package com.example.viettel.feature.identity.domain.entity

import net.sf.scuba.data.Gender

data class MrzData(
    val documentType: String,
    val issuingState: String,
    val primaryIdentifier: String,
    val secondaryIdentifier: String,
    val documentNumber: String,
    val nationality: String,
    val dateOfBirth: String,
    val gender: Gender,
    val dateOfExpiry: String,
    val optionalData1: String,
    val optionalData2: String,
)
