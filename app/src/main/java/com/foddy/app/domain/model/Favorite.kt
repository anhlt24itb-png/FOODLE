package com.foddy.app.domain.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Favorite(
    val id: String = "",
    val userId: String = "",
    val targetId: String = "", // FoodId hoặc RestaurantId
    val targetName: String = "",
    val targetImage: String = "",
    val type: String = "FOOD", // FOOD hoặc RESTAURANT
    val createdAt: Long = System.currentTimeMillis()
)
