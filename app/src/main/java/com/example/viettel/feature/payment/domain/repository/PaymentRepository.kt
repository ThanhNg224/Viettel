package com.example.viettel.feature.payment.domain.repository

import com.example.viettel.feature.payment.domain.entity.PaymentMethod
import com.example.viettel.feature.payment.domain.entity.PaymentStatus
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    fun startPayment(method: PaymentMethod): Flow<PaymentStatus>
    fun cancelPayment()
}
