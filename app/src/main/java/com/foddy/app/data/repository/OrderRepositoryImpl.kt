package com.foddy.app.data.repository

import com.foddy.app.data.local.OrderDao
import com.foddy.app.data.local.OrderEntity
import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.model.DriverLocation
import com.foddy.app.domain.model.OrderChatMessage
import com.foddy.app.domain.repository.OrderRepository
import com.foddy.app.domain.repository.NotificationRepository
import com.foddy.app.domain.repository.OrderEvent
import com.foddy.app.domain.repository.OrderInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val orderDao: OrderDao,
    private val notificationRepository: NotificationRepository
) : OrderRepository {

    private val gson = Gson()

    override fun getPendingOrders(): Flow<List<OrderRequest>> = callbackFlow {
        val listener = firestore.collection("orders")
            .whereIn("status", listOf("PENDING", "CONFIRMED", "PREPARING", "DELIVERING", "COMPLETED"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to pending orders")
                    close(error)
                    return@addSnapshotListener
                }
                try {
                    val orders = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(OrderRequest::class.java)?.copy(id = doc.id)
                    }?.sortedByDescending { it.createdAt } ?: emptyList()
                    trySend(orders)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing pending orders")
                }
            }
        awaitClose { listener.remove() }
    }.distinctUntilChanged()

    override fun getOrdersByRestaurant(restaurantId: String): Flow<List<OrderRequest>> = callbackFlow {
        Timber.d("FIRESTORE_DEBUG: Bắt đầu lắng nghe đơn hàng cho RestaurantID: $restaurantId")
        
        val listener = firestore.collection("orders")
            .whereEqualTo("restaurantId", restaurantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "FIRESTORE_ERROR: Lỗi khi lấy đơn hàng của quán $restaurantId")
                    close(error)
                    return@addSnapshotListener
                }
                
                try {
                    val orders = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(OrderRequest::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Timber.e(e, "FIRESTORE_ERROR: Lỗi parse đơn hàng ${doc.id}")
                            null
                        }
                    } ?: emptyList()
                    
                    Timber.d("FIRESTORE_DEBUG: Tìm thấy ${orders.size} đơn hàng cho quán $restaurantId")
                    trySend(orders)
                } catch (e: Exception) {
                    Timber.e(e, "Error processing restaurant orders snapshot")
                }
            }
        awaitClose { listener.remove() }
    }.distinctUntilChanged()

    override fun getOrdersByUser(userId: String): Flow<List<OrderRequest>> = flow {
        // Emit cached data first
        val cachedOrders = orderDao.getOrdersByUser(userId).first()
        emit(cachedOrders.map { it.toDomainModel() })

        // Then sync from Firestore
        getRemoteOrdersByUser(userId).collect { remoteOrders ->
            orderDao.insertOrders(remoteOrders.map { it.toEntity() })
            emit(remoteOrders)
        }
    }.distinctUntilChanged()

    private fun getRemoteOrdersByUser(userId: String): Flow<List<OrderRequest>> = callbackFlow {
        val listener = firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to user orders")
                    close(error)
                    return@addSnapshotListener
                }
                try {
                    val orders = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(OrderRequest::class.java)?.copy(id = doc.id)
                    }?.sortedByDescending { it.createdAt } ?: emptyList()
                    trySend(orders)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing user orders")
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getOrderById(orderId: String): Flow<OrderRequest?> = flow {
        // Emit cached data first
        val cachedOrder = orderDao.getOrderById(orderId).first()
        emit(cachedOrder?.toDomainModel())

        // Then sync from Firestore
        getRemoteOrderById(orderId).collect { remoteOrder ->
            remoteOrder?.let { orderDao.insertOrder(it.toEntity()) }
            emit(remoteOrder)
        }
    }.distinctUntilChanged()

    private fun getRemoteOrderById(orderId: String): Flow<OrderRequest?> = callbackFlow {
        val listener = firestore.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to order $orderId")
                    close(error)
                    return@addSnapshotListener
                }
                try {
                    val order = snapshot?.toObject(OrderRequest::class.java)?.copy(id = snapshot.id)
                    trySend(order)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing order $orderId")
                }
            }
        awaitClose { listener.remove() }
    }

    private fun OrderEntity.toDomainModel(): OrderRequest {
        val itemType = object : TypeToken<List<com.foddy.app.domain.model.CartItem>>() {}.type
        return OrderRequest(
            id = id,
            userId = userId,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            driverId = driverId,
            driverName = driverName,
            status = status,
            totalPrice = totalPrice,
            shippingFee = shippingFee,
            paymentMethod = paymentMethod,
            address = address,
            lat = lat,
            lng = lng,
            createdAt = createdAt,
            items = gson.fromJson(itemsJson, itemType)
        )
    }

    private fun OrderRequest.toEntity(): OrderEntity {
        return OrderEntity(
            id = id,
            userId = userId,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            driverId = driverId,
            driverName = driverName,
            status = status,
            totalPrice = totalPrice,
            shippingFee = shippingFee,
            paymentMethod = paymentMethod,
            address = address,
            lat = lat,
            lng = lng,
            createdAt = createdAt,
            itemsJson = gson.toJson(items)
        )
    }

    override fun trackDriverLocation(orderId: String): Flow<DriverLocation?> = callbackFlow {
        val listener = firestore.collection("driver_locations").document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error tracking driver location for order $orderId")
                    close(error)
                    return@addSnapshotListener
                }
                try {
                    val location = snapshot?.toObject(DriverLocation::class.java)
                    trySend(location)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing driver location for $orderId")
                }
            }
        awaitClose { listener.remove() }
    }.distinctUntilChanged()

    override fun getChatMessages(orderId: String): Flow<List<OrderChatMessage>> = callbackFlow {
        val listener = firestore.collection("chats")
            .whereEqualTo("orderId", orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to chats for order $orderId")
                    close(error)
                    return@addSnapshotListener
                }
                try {
                    val messages = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(OrderChatMessage::class.java)?.copy(id = doc.id)
                    }?.sortedBy { it.timestamp } ?: emptyList()
                    trySend(messages)
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing chat messages for $orderId")
                }
            }
        awaitClose { listener.remove() }
    }.distinctUntilChanged()

    override suspend fun placeOrder(order: OrderRequest): Result<String> = try {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: order.userId
        val docRef = firestore.collection("orders").document()
        
        // Đảm bảo đơn hàng mới luôn có trạng thái PENDING và timestamp đúng
        val orderWithId = order.copy(
            id = docRef.id, 
            userId = uid,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )

        docRef.set(orderWithId).await()

        // Gửi thông báo cho Nhà hàng
        notificationRepository.sendNewOrderNotification(
            restaurantId = order.restaurantId,
            orderId = docRef.id,
            orderInfo = OrderInfo(
                orderId = docRef.id,
                customerName = "Khách hàng", // Có thể lấy từ UserProfile nếu cần
                items = order.items.map { it.foodItem.name },
                totalPrice = order.totalPrice,
                deliveryAddress = order.address,
                paymentMethod = order.paymentMethod
            )
        )

        Result.success(docRef.id)
    } catch (e: Exception) {
        Timber.e(e, "Error placing order")
        Result.failure(e)
    }

    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> = try {
        val updates = mutableMapOf<String, Any>(
            "status" to status,
            "timestamps.$status" to System.currentTimeMillis()
        )
        
        // Lấy thông tin đơn hàng trước để xử lý
        val orderDoc = firestore.collection("orders").document(orderId).get().await()
        val order = orderDoc.toObject(OrderRequest::class.java)?.copy(id = orderDoc.id)
        
        if (order != null) {
            if (status == "CONFIRMED") {
                firestore.runTransaction { transaction ->
                    // Cập nhật trạng thái đơn hàng
                    transaction.update(firestore.collection("orders").document(orderId), updates)
                    
                    // Trừ số lượng trong kho cho từng món
                    order.items.forEach { cartItem ->
                        if (cartItem.foodItem.id.isNotEmpty()) {
                            val foodRef = firestore.collection("foods").document(cartItem.foodItem.id)
                            transaction.update(foodRef, "stock", com.google.firebase.firestore.FieldValue.increment(-cartItem.quantity.toLong()))
                        }
                    }
                }.await()
                Timber.d("STOCK_DEBUG: Đã trừ kho cho đơn hàng $orderId")
            } else {
                firestore.collection("orders").document(orderId).update(updates).await()
            }

            // Gửi thông báo phù hợp cho khách hàng
            val event = when(status) {
                "CONFIRMED" -> OrderEvent.OrderConfirmed
                "PREPARING" -> OrderEvent.Preparing
                "DELIVERING" -> OrderEvent.Delivering
                "COMPLETED" -> OrderEvent.Delivered(order.driverName ?: "Tài xế")
                else -> null
            }
            
            event?.let { e ->
                notificationRepository.sendOrderStatusNotification(order.userId, orderId, e)
            }
        } else {
            // Trường hợp không tìm thấy order, vẫn thử update trực tiếp
            firestore.collection("orders").document(orderId).update(updates).await()
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error updating order status")
        Result.failure(e)
    }

    override suspend fun acceptOrder(orderId: String, driverId: String, driverName: String, status: String): Result<Unit> = try {
        val updates = mapOf(
            "status" to status,
            "driverId" to driverId,
            "driverName" to driverName,
            "timestamps.$status" to System.currentTimeMillis()
        )
        firestore.collection("orders").document(orderId).update(updates).await()
        
        // Gửi thông báo cho khách hàng
        val orderDoc = firestore.collection("orders").document(orderId).get().await()
        val order = orderDoc.toObject(OrderRequest::class.java)
        order?.let {
            val event = if (status == "DELIVERING") OrderEvent.Delivering else OrderEvent.Preparing
            notificationRepository.sendOrderStatusNotification(
                userId = it.userId,
                orderId = orderId,
                event = event
            )
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error accepting order")
        Result.failure(e)
    }

    override suspend fun updateDriverLocation(orderId: String, lat: Double, lng: Double): Result<Unit> = try {
        val location = DriverLocation(orderId, lat, lng, System.currentTimeMillis())
        firestore.collection("driver_locations").document(orderId).set(location).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error updating driver location")
        Result.failure(e)
    }

    override suspend fun sendChatMessage(message: OrderChatMessage): Result<Unit> = try {
        firestore.collection("chats").add(message).await()
        
        // Gửi thông báo cho người nhận
        // Tìm thông tin người gửi (có thể là tài xế hoặc khách)
        val orderDoc = firestore.collection("orders").document(message.orderId).get().await()
        val order = orderDoc.toObject(OrderRequest::class.java)
        
        val senderName = if (message.senderId == order?.driverId) {
            order.driverName ?: "Tài xế"
        } else {
            "Khách hàng"
        }

        notificationRepository.sendChatNotification(
            receiverId = message.receiverId,
            orderId = message.orderId,
            senderName = senderName,
            message = message.message
        )

        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error sending chat message")
        Result.failure(e)
    }
}
