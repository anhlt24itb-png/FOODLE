package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.CartItem
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.usecase.cart.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getTotalPriceUseCase: GetTotalPriceUseCase
) : ViewModel() {

    val cartItems: StateFlow<List<CartItem>> = getCartItemsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPrice: StateFlow<Double> = getTotalPriceUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(foodItem: FoodItem) {
        viewModelScope.launch {
            addToCartUseCase(foodItem)
        }
    }

    fun removeFromCart(foodItem: FoodItem) {
        viewModelScope.launch {
            removeFromCartUseCase(foodItem)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            clearCartUseCase()
        }
    }
}
