package com.example.viettel.feature.payment.domain.usecase

import com.example.viettel.feature.payment.domain.entity.PaymentMethod
import com.example.viettel.feature.payment.domain.repository.PaymentRepository
import javax.inject.Inject

class StartPaymentUseCase @Inject constructor(
    private val repository: PaymentRepository,
) {
    operator fun invoke(method: PaymentMethod) = repository.startPayment(method)
}
