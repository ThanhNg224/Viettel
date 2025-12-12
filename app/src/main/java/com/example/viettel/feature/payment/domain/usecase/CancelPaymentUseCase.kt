package com.example.viettel.feature.payment.domain.usecase

import com.example.viettel.feature.payment.domain.repository.PaymentRepository
import javax.inject.Inject

class CancelPaymentUseCase @Inject constructor(
    private val repository: PaymentRepository,
) {
    operator fun invoke() = repository.cancelPayment()
}
