package com.foddy.app.domain.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class OrderChatMessage(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val orderId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
