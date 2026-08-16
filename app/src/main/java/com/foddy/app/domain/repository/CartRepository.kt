package com.foddy.app.domain.repository

import com.foddy.app.domain.model.CartItem
import com.foddy.app.domain.model.FoodItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    suspend fun addToCart(foodItem: FoodItem)
    suspend fun removeFromCart(foodItem: FoodItem)
    suspend fun clearCart()
    fun getTotalPrice(): Flow<Double>
}
