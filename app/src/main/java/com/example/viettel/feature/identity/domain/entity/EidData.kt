package com.example.viettel.feature.identity.domain.entity

data class EidData(
    val personalInfo: EidPersonalInfo?,
    val chipPortrait: ByteArray?,
    val certificateSummary: String?,
    val verificationSummary: String?,
)
