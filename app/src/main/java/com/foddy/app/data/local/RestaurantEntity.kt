package com.foddy.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val image: String,
    val rating: Double,
    val reviewCount: Int,
    val ownerId: String,
    val open: Boolean,
    val lat: Double,
    val lng: Double,
    val category: String,
    val deliveryTime: String,
    val distance: Double,
    val shippingFee: Double,
    val promoTags: String // Room doesn't support List directly, store as comma-separated string for simplicity or use TypeConverter
)
