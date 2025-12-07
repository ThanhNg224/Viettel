package com.example.viettel.feature.payment.domain.usecase

import com.example.viettel.feature.payment.domain.repository.PaymentRepository

class CancelPaymentUseCase(
    private val repository: PaymentRepository,
) {
    operator fun invoke() = repository.cancelPayment()
}
