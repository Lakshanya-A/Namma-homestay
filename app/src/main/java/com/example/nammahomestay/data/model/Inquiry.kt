package com.example.nammahomestay.data.model

data class Inquiry(
    val id: String = "",
    val customerId: String = "",
    val name: String = "",
    val phone: String = "",
    val message: String = "",
    val homestayId: String = "",
    val hostId: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0
)
