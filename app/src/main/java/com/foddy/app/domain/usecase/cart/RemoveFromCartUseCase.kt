package com.foddy.app.domain.usecase.cart

import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.repository.CartRepository
import javax.inject.Inject

class RemoveFromCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(foodItem: FoodItem) = repository.removeFromCart(foodItem)
}
