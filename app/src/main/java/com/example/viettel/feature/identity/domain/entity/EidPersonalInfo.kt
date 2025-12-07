package com.example.viettel.feature.identity.domain.entity

data class EidPersonalInfo(
    val fullName: String?,
    val documentNumber: String?,
    val personalIdentification: String?,
    val dateOfBirth: String?,
    val gender: String?,
    val nationality: String?,
    val fatherName: String?,
    val motherName: String?,
    val placeOfOrigin: String?,
    val placeOfResidence: String?,
    val religion: String?,
    val ethnicity: String?,
    val dateOfIssue: String?,
    val dateOfExpiry: String?,
)
