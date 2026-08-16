package com.foddy.app.domain.repository

import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.model.DriverLocation
import com.foddy.app.domain.model.OrderChatMessage
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getPendingOrders(): Flow<List<OrderRequest>>
    fun getOrdersByRestaurant(restaurantId: String): Flow<List<OrderRequest>>
    fun getOrdersByUser(userId: String): Flow<List<OrderRequest>>
    fun getOrderById(orderId: String): Flow<OrderRequest?>
    fun trackDriverLocation(orderId: String): Flow<DriverLocation?>
    fun getChatMessages(orderId: String): Flow<List<OrderChatMessage>>
    suspend fun placeOrder(order: OrderRequest): Result<String>
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit>
    suspend fun acceptOrder(orderId: String, driverId: String, driverName: String, status: String): Result<Unit>
    suspend fun updateDriverLocation(orderId: String, lat: Double, lng: Double): Result<Unit>
    suspend fun sendChatMessage(message: OrderChatMessage): Result<Unit>
}
