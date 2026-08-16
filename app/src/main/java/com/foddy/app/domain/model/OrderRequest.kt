package com.foddy.app.domain.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class OrderRequest(
    val id: String = "",
    val userId: String = "",
    val restaurantId: String = "",
    val restaurantName: String = "",
    val driverId: String? = null,
    val driverName: String? = null,
    val status: String = "PENDING",
    val totalPrice: Double = 0.0,
    val shippingFee: Double = 0.0,
    val paymentMethod: String = "CASH",
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val items: List<CartItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val driverLocation: Location? = null,
    val customerLocation: Location? = null,
    val timestamps: Map<String, Long> = emptyMap()
)
