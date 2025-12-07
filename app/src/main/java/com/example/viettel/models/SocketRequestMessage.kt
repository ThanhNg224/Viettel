package com.example.viettel.models

data class SocketRequestMessage(
    val actionType: String = "",
    val data: String? = null,
    val requestId: String? = null,
)
