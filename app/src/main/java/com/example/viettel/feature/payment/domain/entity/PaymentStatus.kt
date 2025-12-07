package com.example.viettel.feature.payment.domain.entity

sealed class PaymentStatus {
    object Idle : PaymentStatus()
    object Processing : PaymentStatus()
    object Success : PaymentStatus()
    data class Error(val message: String) : PaymentStatus()
    object Cancelled : PaymentStatus()
}
