package com.example.viettel.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.viettel.feature.payment.data.repository.PaymentRepositoryImpl
import com.example.viettel.feature.payment.domain.usecase.CancelPaymentUseCase
import com.example.viettel.feature.payment.domain.usecase.StartPaymentUseCase
import com.example.viettel.feature.payment.presentation.PaymentViewModel

class PaymentViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = PaymentRepositoryImpl()
        val startUseCase = StartPaymentUseCase(repository)
        val cancelUseCase = CancelPaymentUseCase(repository)
        return PaymentViewModel(startUseCase, cancelUseCase) as T
    }
}

