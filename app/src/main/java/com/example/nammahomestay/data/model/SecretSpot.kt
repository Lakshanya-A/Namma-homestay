package com.example.nammahomestay.data.model

data class SecretSpot(
    val id: String = "",
    val hostId: String = "",
    val name: String = "",
    val type: String = "", // Waterfall, Viewpoint, etc.
    val description: String = "",
    val imageUrl: String = ""
)
