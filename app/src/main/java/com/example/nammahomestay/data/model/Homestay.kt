package com.example.nammahomestay.data.model

data class Homestay(
    val id: String = "",
    val hostId: String = "",
    val name: String = "",
    val location: String = "",
    val price: String = "",
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val nearbySpots: List<NearbySpot> = emptyList()
)

data class NearbySpot(
    val name: String = "",
    val type: String = "",
    val description: String = ""
)
