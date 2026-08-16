package com.foddy.app.domain.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

enum class UserRole {
    CUSTOMER,
    DRIVER,
    RESTAURANT_OWNER,
    ADMIN,
    USER
}

@IgnoreExtraProperties
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    var role: String = UserRole.CUSTOMER.name,
    val phone: String = "",
    val address: String = "",
    val avatar: String = "",
    val restaurantId: String? = null,
    val status: String = "ACTIVE",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("isLoggedIn")
    @set:PropertyName("isLoggedIn")
    var isLoggedIn: Boolean = false,
    val fcmToken: String? = null
) {
    val userRole: UserRole
        get() = try {
            UserRole.valueOf(role.uppercase())
        } catch (e: Exception) {
            UserRole.CUSTOMER
        }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "role" to role,
            "phone" to phone,
            "address" to address,
            "avatar" to avatar,
            "restaurantId" to restaurantId,
            "status" to status,
            "isActive" to isActive,
            "createdAt" to createdAt,
            "isLoggedIn" to isLoggedIn,
            "fcmToken" to fcmToken
        )
    }
}
