package com.example.viettel.feature.feedback.data.repository

import com.example.viettel.feature.feedback.domain.entity.FeedbackData
import com.example.viettel.feature.feedback.domain.repository.FeedbackRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation của FeedbackRepository.
 * Xử lý việc gửi feedback của người dùng.
 */
@Singleton
class FeedbackRepositoryImpl @Inject constructor() : FeedbackRepository {
    override suspend fun submitFeedback(data: FeedbackData) {
        // TODO: Implement actual API call
        // Simulate sending feedback
        delay(500)
    }
}
