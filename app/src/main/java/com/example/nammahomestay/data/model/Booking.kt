package com.example.nammahomestay.data.model

data class Booking(
    val id: String = "",
    val homestayId: String = "",
    val homestayName: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val hostId: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val status: String = "PENDING", // PENDING, CONFIRMED, REJECTED
    val timestamp: Long = System.currentTimeMillis()
)
