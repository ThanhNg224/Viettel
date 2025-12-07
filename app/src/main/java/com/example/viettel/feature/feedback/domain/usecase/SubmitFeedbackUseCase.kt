package com.example.viettel.feature.feedback.domain.usecase

import com.example.viettel.feature.feedback.domain.entity.FeedbackData
import com.example.viettel.feature.feedback.domain.repository.FeedbackRepository
import javax.inject.Inject

/**
 * UseCase xử lý việc submit feedback của người dùng.
 */
class SubmitFeedbackUseCase @Inject constructor(
    private val repository: FeedbackRepository,
) {
    suspend operator fun invoke(data: FeedbackData) {
        repository.submitFeedback(data)
    }
}
