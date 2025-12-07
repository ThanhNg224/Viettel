package com.example.viettel.feature.identity.domain.entity

data class FaceAttributes(
    val smilingProbability: Float,
    val leftEyeOpenProbability: Float,
    val rightEyeOpenProbability: Float,
    val headYaw: Float,
)
