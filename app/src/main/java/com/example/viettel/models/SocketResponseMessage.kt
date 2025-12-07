package com.example.viettel.models

data class SocketResponseMessage<T>(
    val actionType: String = "",
    val status: String = "",
    val data: T? = null,
    val requestId: String? = null,
)
