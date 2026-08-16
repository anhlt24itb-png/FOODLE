package com.foddy.app.domain.model

import androidx.compose.runtime.Immutable
import com.google.firebase.firestore.IgnoreExtraProperties

@Immutable
@IgnoreExtraProperties
data class FoodItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val restaurantId: String = "",
    val category: String = "",
    val available: Boolean = true,
    val discountPrice: Double? = null,
    val rating: Double = 0.0,
    val isFlashSale: Boolean = false,
    val soldCount: Int = 0,
    val stock: Int = 99, // Thêm trường số lượng trong kho
    val calories: Int = 0
)

@Immutable
@IgnoreExtraProperties
data class Restaurant(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val image: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val ownerId: String = "",
    val open: Boolean = false,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val category: String = "",
    val deliveryTime: String = "20-30 min",
    val distance: Double = 0.0,
    val shippingFee: Double = 15000.0,
    val promoTags: List<String> = emptyList(),
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)

data class CartItem(
    val foodItem: FoodItem = FoodItem(),
    var quantity: Int = 0
)

@IgnoreExtraProperties
data class DriverLocation(
    val driverId: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Immutable
@IgnoreExtraProperties
data class Driver(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val avatar: String = "",
    val rating: Double = 5.0,
    val vehicleInfo: String = "",
    val online: Boolean = false,
    val currentOrderId: String = "",
    val totalOrders: Int = 0,
    val earnings: Double = 0.0,
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis(),
    val lat: Double? = null,
    val lng: Double? = null
)
