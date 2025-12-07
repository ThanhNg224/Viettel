package com.example.viettel.feature.payment.data.repository

import com.example.viettel.feature.payment.domain.entity.PaymentMethod
import com.example.viettel.feature.payment.domain.entity.PaymentStatus
import com.example.viettel.feature.payment.domain.repository.PaymentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PaymentRepositoryImpl : PaymentRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val statusFlow = MutableSharedFlow<PaymentStatus>(replay = 1)
    private var currentJob: Job? = null

    override fun startPayment(method: PaymentMethod): Flow<PaymentStatus> {
        currentJob?.cancel()
        statusFlow.tryEmit(PaymentStatus.Processing)
        currentJob = scope.launch {
            delay(10_000) // simulate QR countdown
            statusFlow.emit(PaymentStatus.Success)
        }
        return statusFlow.asSharedFlow()
    }

    override fun cancelPayment() {
        currentJob?.cancel()
        statusFlow.tryEmit(PaymentStatus.Cancelled)
    }
}
