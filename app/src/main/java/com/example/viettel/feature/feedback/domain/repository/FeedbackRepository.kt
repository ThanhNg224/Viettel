package com.example.viettel.feature.feedback.domain.repository

import com.example.viettel.feature.feedback.domain.entity.FeedbackData

interface FeedbackRepository {
    suspend fun submitFeedback(data: FeedbackData)
}
