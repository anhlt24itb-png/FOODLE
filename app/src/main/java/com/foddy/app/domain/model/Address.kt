package com.foddy.app.domain.model

data class Address(
    val id: String = "",
    val userId: String = "",
    val label: String = "Nhà", // Nhà, Công ty, Khác
    val fullAddress: String = "",
    val receiverName: String = "",
    val receiverPhone: String = "",
    val isDefault: Boolean = false,
    val lat: Double = 0.0,
    val lng: Double = 0.0
)
