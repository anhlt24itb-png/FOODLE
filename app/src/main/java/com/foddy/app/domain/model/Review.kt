package com.foddy.app.domain.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val orderId: String = "",
    val targetId: String = "", // FoodId hoặc RestaurantId
    val rating: Int = 5,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
