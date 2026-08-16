package com.foddy.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val foodId: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val quantity: Int,
    val restaurantId: String = ""
)
