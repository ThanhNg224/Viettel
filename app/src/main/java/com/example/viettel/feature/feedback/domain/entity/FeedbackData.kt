package com.example.viettel.feature.feedback.domain.entity

data class FeedbackData(
    val rating: Int,
    val reasons: Set<FeedbackReason>,
    val customReason: String?,
)
