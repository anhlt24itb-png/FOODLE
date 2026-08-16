package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.*
import com.foddy.app.domain.repository.OrderEvent
import com.foddy.app.domain.usecase.cart.ClearCartUseCase
import com.foddy.app.domain.usecase.cart.GetCartItemsUseCase
import com.foddy.app.domain.usecase.cart.GetTotalPriceUseCase
import com.foddy.app.domain.usecase.notification.SendOrderStatusNotificationUseCase
import com.foddy.app.domain.usecase.order.PlaceOrderUseCase
import com.foddy.app.domain.usecase.user.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// 1. State: Đại diện cho toàn bộ dữ liệu trên màn hình Checkout
data class CheckoutUiState(
    val isLoading: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val customerId: String = "",
    val customerName: String = "",
    val deliveryAddress: String = "",
    val selectedLat: Double = 0.0,
    val selectedLng: Double = 0.0,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val orderId: String = "",
    val bankAccount: BankAccount? = null,
    val error: String? = null
)

// 2. Effect: Xử lý các sự kiện một lần như Navigation hoặc Show Snackbar
sealed class CheckoutUiEffect {
    object NavigateToSuccess : CheckoutUiEffect()
    data class ShowError(val message: String) : CheckoutUiEffect()
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val getCartItemsUseCase: GetCartItemsUseCase,
    private val getTotalPriceUseCase: GetTotalPriceUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val sendOrderStatusNotificationUseCase: SendOrderStatusNotificationUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getAddressesUseCase: com.foddy.app.domain.usecase.address.GetAddressesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<CheckoutUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        observeCartAndUser()
        generateOrderDetails()
    }

    private fun observeCartAndUser() {
        // Observe Cart Items
        viewModelScope.launch {
            getCartItemsUseCase().collectLatest { items ->
                _uiState.update { it.copy(cartItems = items) }
            }
        }
        // Observe Total Price
        viewModelScope.launch {
            getTotalPriceUseCase().collectLatest { total ->
                _uiState.update { it.copy(totalPrice = total) }
            }
        }
        // Observe User Profile
        viewModelScope.launch {
            getCurrentUserUseCase().collectLatest { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            customerId = it.id,
                            customerName = it.name
                        )
                    }
                }
            }
        }
        // Observe Default Address
        viewModelScope.launch {
            getAddressesUseCase().collectLatest { addresses ->
                val defaultAddress = addresses.find { it.isDefault } ?: addresses.firstOrNull()
                defaultAddress?.let { addr ->
                    _uiState.update { 
                        it.copy(
                            deliveryAddress = addr.fullAddress,
                            selectedLat = addr.lat,
                            selectedLng = addr.lng
                        )
                    }
                }
            }
        }
    }

    private fun generateOrderDetails() {
        _uiState.update { 
            it.copy(
                orderId = "FD-${UUID.randomUUID().toString().take(6).uppercase()}",
                bankAccount = BankAccount(
                    bankName = "MB Bank",
                    bankCode = "MB",
                    accountNumber = "0345678999",
                    accountName = "FODDY APP SYSTEM"
                )
            )
        }
    }

    fun onAddressChanged(newAddress: String) {
        _uiState.update { it.copy(deliveryAddress = newAddress, error = null) }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun simulatePaymentSuccess() {
        if (_uiState.value.selectedPaymentMethod == PaymentMethod.BANK_TRANSFER) {
            _uiState.update { it.copy(paymentStatus = PaymentStatus.PAID) }
        }
    }

    fun placeOrder() {
        val currentState = _uiState.value
        
        if (currentState.deliveryAddress.isBlank()) {
            _uiState.update { it.copy(error = "Vui lòng nhập địa chỉ giao hàng") }
            return
        }

        if (currentState.cartItems.isEmpty()) {
            _uiState.update { it.copy(error = "Giỏ hàng của bạn đang trống") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Lấy restaurantId từ món ăn đầu tiên, ưu tiên ID thực, nếu không có mới dùng mặc định
                val firstItem = currentState.cartItems.firstOrNull()
                val targetRestaurantId = firstItem?.foodItem?.restaurantId?.takeIf { it.isNotBlank() } ?: "res_001"
                
                val order = OrderRequest(
                    id = currentState.orderId,
                    userId = currentState.customerId,
                    restaurantId = targetRestaurantId,
                    restaurantName = "Đơn hàng từ hệ thống",
                    address = currentState.deliveryAddress,
                    lat = currentState.selectedLat,
                    lng = currentState.selectedLng,
                    totalPrice = currentState.totalPrice,
                    items = currentState.cartItems,
                    paymentMethod = when(currentState.selectedPaymentMethod) {
                        PaymentMethod.CASH_ON_DELIVERY -> "COD"
                        PaymentMethod.BANK_TRANSFER -> "VietQR"
                    },
                    status = OrderStatus.PENDING.name,
                    customerLocation = Location(currentState.selectedLat, currentState.selectedLng)
                )

                placeOrderUseCase(order)
                    .onSuccess { orderId ->
                        sendOrderStatusNotificationUseCase(
                            userId = currentState.customerId,
                            orderId = orderId,
                            event = OrderEvent.OrderConfirmed
                        )

                        clearCartUseCase()

                        _uiState.update { it.copy(isLoading = false, paymentStatus = PaymentStatus.PAID) }
                        viewModelScope.launch {
                            _uiEffect.send(CheckoutUiEffect.NavigateToSuccess)
                        }
                    }
                    .onFailure { e ->
                        com.foddy.app.util.AppLogger.e(e, "Lỗi khi đặt hàng tại Repository")
                        _uiState.update { it.copy(isLoading = false, error = "Lỗi hệ thống: ${e.message}") }
                        viewModelScope.launch {
                            _uiEffect.send(CheckoutUiEffect.ShowError("Không thể gửi đơn hàng: ${e.message}"))
                        }
                    }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Đặt hàng thất bại: ${e.message}") }
                _uiEffect.send(CheckoutUiEffect.ShowError(e.message ?: "Lỗi hệ thống, vui lòng thử lại"))
            }
        }
    }
}
