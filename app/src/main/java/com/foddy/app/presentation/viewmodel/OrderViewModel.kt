package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.model.DriverLocation
import com.foddy.app.domain.model.OrderChatMessage
import com.foddy.app.domain.repository.OrderRepository
import com.foddy.app.core.location.DriverLocationManager
import android.location.Location
import com.foddy.app.presentation.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val locationManager: DriverLocationManager
) : ViewModel() {

    private val _orderState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val orderState: StateFlow<UiState<String>> = _orderState.asStateFlow()

    private val _userOrders = MutableStateFlow<UiState<List<OrderRequest>>>(UiState.Idle)
    val userOrders: StateFlow<UiState<List<OrderRequest>>> = _userOrders.asStateFlow()

    private val _restaurantOrders = MutableStateFlow<List<OrderRequest>>(emptyList())
    val restaurantOrders: StateFlow<List<OrderRequest>> = _restaurantOrders.asStateFlow()

    private val _pendingOrders = MutableStateFlow<List<OrderRequest>>(emptyList())
    val pendingOrders: StateFlow<List<OrderRequest>> = _pendingOrders.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _orderIdFlow = MutableStateFlow<String?>(null)

    val currentOrder: StateFlow<OrderRequest?> = _orderIdFlow
        .filterNotNull()
        .flatMapLatest { id -> 
            orderRepository.getOrderById(id)
                .catch { e -> println("Error getting order by id: ${e.message}") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val chatMessages: StateFlow<List<OrderChatMessage>> = _orderIdFlow
        .filterNotNull()
        .flatMapLatest { id -> 
            orderRepository.getChatMessages(id)
                .catch { e -> println("Error getting chat messages: ${e.message}") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val driverLocation: StateFlow<DriverLocation?> = currentOrder
        .flatMapLatest { order ->
            if (order != null && order.status != "PENDING") {
                orderRepository.trackDriverLocation(order.id)
                    .catch { e -> println("Error tracking driver location: ${e.message}") }
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun trackOrder(orderId: String) {
        _orderIdFlow.value = orderId
    }

    fun listenToUserOrders(userId: String) {
        viewModelScope.launch {
            _userOrders.value = UiState.Loading
            orderRepository.getOrdersByUser(userId)
                .catch { e ->
                    _userOrders.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect {
                    _userOrders.value = UiState.Success(it)
                }
        }
    }

    fun listenToRestaurantOrders(restaurantId: String) {
        viewModelScope.launch {
            orderRepository.getOrdersByRestaurant(restaurantId)
                .catch { e -> println("Error listening to restaurant orders: ${e.message}") }
                .collect {
                    _restaurantOrders.value = it
                }
        }
    }

    fun listenToPendingOrders() {
        viewModelScope.launch {
            orderRepository.getPendingOrders()
                .catch { e -> println("Error listening to pending orders: ${e.message}") }
                .collect {
                    _pendingOrders.value = it
                }
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            _orderState.value = UiState.Loading
            orderRepository.updateOrderStatus(orderId, status)
                .onSuccess {
                    _orderState.value = UiState.Success("Cập nhật trạng thái thành công")
                }
                .onFailure { e ->
                    _orderState.value = UiState.Error(e.message ?: "Lỗi khi cập nhật trạng thái")
                }
        }
    }

    fun acceptOrder(orderId: String, driverId: String, driverName: String) {
        viewModelScope.launch {
            _orderState.value = UiState.Loading
            orderRepository.acceptOrder(orderId, driverId, driverName, "PREPARING")
                .onSuccess {
                    _orderState.value = UiState.Success("Đã nhận đơn hàng thành công")
                }
                .onFailure { e ->
                    _orderState.value = UiState.Error(e.message ?: "Lỗi khi nhận đơn hàng")
                }
        }
    }

    fun resetOrderState() {
        _orderState.value = UiState.Idle
    }

    fun updateLocation(orderId: String, lat: Double, lng: Double) {
        val newLocation = Location("").apply {
            latitude = lat
            longitude = lng
        }

        if (locationManager.shouldUpdateLocation(newLocation)) {
            viewModelScope.launch {
                orderRepository.updateDriverLocation(orderId, lat, lng)
            }
        }
    }

    fun sendChatMessage(message: String, senderId: String, receiverId: String, orderId: String) {
        viewModelScope.launch {
            val chatMsg = OrderChatMessage(
                senderId = senderId,
                receiverId = receiverId,
                orderId = orderId,
                message = message,
                timestamp = System.currentTimeMillis()
            )
            orderRepository.sendChatMessage(chatMsg)
        }
    }

    private var simulationJob: kotlinx.coroutines.Job? = null

    fun startLocationSimulation(orderId: String) {
        if (_isSimulating.value) {
            stopLocationSimulation()
            return
        }
        
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            _isSimulating.value = true
            try {
                val startLat = 21.0285
                val startLng = 105.8542
                val endLat = 21.0333
                val endLng = 105.8444
                
                var steps = 0
                val totalSteps = 20
                
                while (steps <= totalSteps && _isSimulating.value) {
                    val currentLat = startLat + (endLat - startLat) * (steps.toDouble() / totalSteps)
                    val currentLng = startLng + (endLng - startLng) * (steps.toDouble() / totalSteps)
                    
                    updateLocation(orderId, currentLat, currentLng)
                    
                    steps++
                    kotlinx.coroutines.delay(2000)
                }
            } finally {
                _isSimulating.value = false
            }
        }
    }

    fun stopLocationSimulation() {
        _isSimulating.value = false
        simulationJob?.cancel()
    }
}
