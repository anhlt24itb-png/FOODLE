package com.foddy.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val discountPrice: Double?,
    val imageUrl: String,
    val rating: Double,
    val calories: Int,
    val isFlashSale: Boolean,
    val restaurantId: String,
    val category: String,
    val available: Boolean,
    val soldCount: Int
)
