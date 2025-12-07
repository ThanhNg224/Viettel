package com.example.viettel.feature.feedback.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viettel.feature.feedback.domain.entity.FeedbackData
import com.example.viettel.feature.feedback.domain.entity.FeedbackReason
import com.example.viettel.feature.feedback.domain.usecase.SubmitFeedbackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel quản lý state và business logic cho feedback flow.
 * Sử dụng Hilt để inject dependencies tự động.
 */
@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    fun setRating(rating: Int) {
        _uiState.value = _uiState.value.copy(rating = rating, submissionCompleted = false)
    }

    fun toggleReason(reason: FeedbackReason, checked: Boolean) {
        val updated = _uiState.value.selectedReasons.toMutableSet().apply {
            if (checked) add(reason) else remove(reason)
        }
        _uiState.value = _uiState.value.copy(selectedReasons = updated)
    }

    fun updateCustomReason(text: String) {
        _uiState.value = _uiState.value.copy(customReason = text)
    }

    fun isFeedbackValid(): Boolean {
        return _uiState.value.rating > 0 && (
            _uiState.value.rating > 4 || _uiState.value.selectedReasons.isNotEmpty()
            )
    }

    fun submitFeedback(onDone: (() -> Unit)? = null) {
        val state = _uiState.value
        if (!isFeedbackValid()) return

        viewModelScope.launch {
            val data = FeedbackData(
                rating = state.rating,
                reasons = state.selectedReasons,
                customReason = state.customReason
            )
            submitFeedbackUseCase(data)
            _uiState.value = state.copy(submissionCompleted = true)
            onDone?.invoke()
        }
    }

    data class FeedbackUiState(
        val rating: Int = 0,
        val selectedReasons: Set<FeedbackReason> = emptySet(),
        val customReason: String? = null,
        val submissionCompleted: Boolean = false,
    )
}
