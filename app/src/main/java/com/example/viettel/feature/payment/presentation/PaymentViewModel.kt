package com.example.viettel.feature.payment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.viettel.feature.payment.data.repository.PaymentRepositoryImpl
import com.example.viettel.feature.payment.domain.entity.PaymentMethod
import com.example.viettel.feature.payment.domain.entity.PaymentStatus
import com.example.viettel.feature.payment.domain.usecase.CancelPaymentUseCase
import com.example.viettel.feature.payment.domain.usecase.StartPaymentUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val startPaymentUseCase: StartPaymentUseCase,
    private val cancelPaymentUseCase: CancelPaymentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState

    private var paymentJob: Job? = null

    fun selectMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(selectedMethod = method)
    }

    fun startPaymentIfNeeded() {
        val method = _uiState.value.selectedMethod ?: return
        if (_uiState.value.status == PaymentStatus.Processing) return

        paymentJob?.cancel()
        paymentJob = viewModelScope.launch {
            startPaymentUseCase(method).collect { status ->
                _uiState.value = _uiState.value.copy(status = status)
            }
        }
    }

    fun cancelPayment() {
        cancelPaymentUseCase()
        _uiState.value = _uiState.value.copy(status = PaymentStatus.Cancelled)
    }

    data class PaymentUiState(
        val selectedMethod: PaymentMethod? = null,
        val status: PaymentStatus = PaymentStatus.Idle,
    )

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repository = PaymentRepositoryImpl()
            val startUseCase = StartPaymentUseCase(repository)
            val cancelUseCase = CancelPaymentUseCase(repository)
            return PaymentViewModel(startUseCase, cancelUseCase) as T
        }
    }
}
