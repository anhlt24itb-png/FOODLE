package com.foddy.app.data.repository

import com.foddy.app.data.local.CartDao
import com.foddy.app.data.local.CartItemEntity
import com.foddy.app.domain.model.CartItem
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getCartItems().map { entities ->
            entities.map { entity ->
                CartItem(
                    foodItem = FoodItem(
                        id = entity.foodId,
                        name = entity.name,
                        price = entity.price,
                        imageUrl = entity.imageUrl,
                        restaurantId = entity.restaurantId
                    ),
                    quantity = entity.quantity
                )
            }
        }
    }

    override suspend fun addToCart(foodItem: FoodItem) {
        val existingItem = cartDao.getCartItemById(foodItem.id)
        if (existingItem != null) {
            cartDao.updateCartItem(existingItem.copy(quantity = existingItem.quantity + 1))
        } else {
            cartDao.addToCart(
                CartItemEntity(
                    foodId = foodItem.id,
                    name = foodItem.name,
                    price = foodItem.price,
                    imageUrl = foodItem.imageUrl,
                    quantity = 1,
                    restaurantId = foodItem.restaurantId
                )
            )
        }
    }

    override suspend fun removeFromCart(foodItem: FoodItem) {
        val existingItem = cartDao.getCartItemById(foodItem.id)
        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                cartDao.updateCartItem(existingItem.copy(quantity = existingItem.quantity - 1))
            } else {
                cartDao.removeFromCart(foodItem.id)
            }
        }
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }

    override fun getTotalPrice(): Flow<Double> {
        return getCartItems().map { items ->
            items.sumOf { it.foodItem.price * it.quantity }
        }
    }
}
