package com.foddy.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val restaurantId: String,
    val restaurantName: String,
    val driverId: String?,
    val driverName: String?,
    val status: String,
    val totalPrice: Double,
    val shippingFee: Double,
    val paymentMethod: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val createdAt: Long,
    val itemsJson: String // Lưu list items dưới dạng JSON để đơn giản hóa
)
