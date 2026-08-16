package com.foddy.app.util

import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

object SeedData {
    private val db = FirebaseFirestore.getInstance()
    private const val USER_UID = "eEhBaQc6OtQHltzRIp1KmJUnGPn1"

    fun seedDatabase() {
        try {
            db.collection("addresses")
                .whereEqualTo("userId", USER_UID)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) {
                        performSeed()
                    }
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Error checking addresses for seed")
                }
        } catch (e: Exception) {
            Timber.e(e, "Exception in seedDatabase")
        }
    }

    private fun performSeed() {
        // Address
        val address = hashMapOf(
            "id" to "address_001",
            "userId" to USER_UID,
            "label" to "Nhà",
            "fullAddress" to "123 Nguyễn Văn Linh, Đà Nẵng",
            "lat" to 16.0678,
            "lng" to 108.2208,
            "isDefault" to true
        )
        db.collection("addresses").document("address_001").set(address)

        // Favorite
        val favorite = hashMapOf(
            "id" to "fav_001",
            "userId" to USER_UID,
            "targetId" to "food_1",
            "type" to "FOOD"
        )
        db.collection("favorites").document("fav_001").set(favorite)

        // Review
        val review = hashMapOf(
            "id" to "rev_001",
            "userId" to USER_UID,
            "userName" to "Người dùng mẫu",
            "targetId" to "food_1",
            "type" to "FOOD",
            "rating" to 5,
            "comment" to "Món ăn cực kỳ ngon, giao hàng nhanh!",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("reviews").document("rev_001").set(review)

        // Notification
        val notification = hashMapOf(
            "id" to "noti_001",
            "userId" to USER_UID,
            "title" to "Chào mừng!",
            "body" to "Chào mừng bạn đến với FOODLE App.",
            "type" to "GENERAL",
            "isRead" to false,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("notifications").document("noti_001").set(notification)

        // Order
        val order = hashMapOf(
            "id" to "order_001",
            "userId" to USER_UID,
            "restaurantId" to "restaurant_1",
            "restaurantName" to "Hương Vị Việt",
            "status" to "pending",
            "totalPrice" to 250000.0,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("orders").document("order_001").set(order)
        
        Timber.d("Seed data successfully added to Firestore")
    }
}
