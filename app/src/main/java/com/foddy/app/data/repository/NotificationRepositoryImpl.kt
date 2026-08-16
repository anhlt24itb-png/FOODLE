package com.foddy.app.data.repository

import com.foddy.app.domain.repository.Notification
import com.foddy.app.domain.repository.NotificationRepository
import com.foddy.app.domain.repository.NotificationType
import com.foddy.app.domain.repository.OrderEvent
import com.foddy.app.domain.repository.OrderInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : NotificationRepository {

    override fun observeNotifications(): Flow<List<Notification>> = callbackFlow {
        val userId = firebaseAuth.currentUser?.uid ?: ""
        if (userId.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val listener = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to notifications")
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notification::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(notifications)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun sendNewOrderNotification(
        restaurantId: String,
        orderId: String,
        orderInfo: OrderInfo
    ): Result<Unit> = try {
        val notification = Notification(
            userId = restaurantId,
            title = "Đơn hàng mới!",
            body = "Bạn có đơn hàng mới từ ${orderInfo.customerName}",
            type = NotificationType.NEW_ORDER,
            orderId = orderId,
            timestamp = System.currentTimeMillis()
        )
        firestore.collection("notifications").add(notification).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error sending new order notification")
        Result.failure(e)
    }

    override suspend fun sendNewDeliveryNotification(
        shipperId: String,
        orderId: String,
        orderInfo: OrderInfo
    ): Result<Unit> = try {
        val notification = Notification(
            userId = shipperId,
            title = "Chuyến hàng mới!",
            body = "Giao đơn hàng đến ${orderInfo.deliveryAddress}",
            type = NotificationType.NEW_DELIVERY,
            orderId = orderId,
            timestamp = System.currentTimeMillis()
        )
        firestore.collection("notifications").add(notification).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error sending new delivery notification")
        Result.failure(e)
    }

    override suspend fun sendOrderStatusNotification(
        userId: String,
        orderId: String,
        event: OrderEvent
    ): Result<Unit> = try {
        val (title, body) = when (event) {
            is OrderEvent.OrderConfirmed -> "Đã xác nhận" to "Đơn hàng của bạn đã được xác nhận"
            is OrderEvent.Preparing -> "Đang chuẩn bị" to "Nhà hàng đang chuẩn bị món ăn cho bạn"
            is OrderEvent.ReadyForPickup -> "Sẵn sàng giao" to "Đơn hàng đã sẵn sàng để giao đi"
            is OrderEvent.Delivering -> "Đang giao hàng" to "Tài xế đang trên đường giao hàng cho bạn"
            is OrderEvent.Delivered -> "Đã giao hàng" to "Đơn hàng của bạn đã được giao thành công bởi ${event.shipperName}"
            is OrderEvent.Cancelled -> "Đã hủy" to "Đơn hàng bị hủy: ${event.reason}"
        }

        val notification = Notification(
            userId = userId,
            title = title,
            body = body,
            type = NotificationType.ORDER_STATUS,
            orderId = orderId,
            timestamp = System.currentTimeMillis()
        )
        firestore.collection("notifications").add(notification).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error sending order status notification")
        Result.failure(e)
    }

    override suspend fun sendPaymentConfirmedNotification(
        restaurantId: String,
        orderId: String,
        amount: Double
    ): Result<Unit> = try {
        val notification = Notification(
            userId = restaurantId,
            title = "Thanh toán thành công",
            body = "Đơn hàng #$orderId đã được thanh toán: ${amount.toInt()}đ",
            type = NotificationType.PAYMENT_CONFIRMED,
            orderId = orderId,
            timestamp = System.currentTimeMillis()
        )
        firestore.collection("notifications").add(notification).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error sending payment confirmed notification")
        Result.failure(e)
    }

    override suspend fun sendChatNotification(
        receiverId: String,
        orderId: String,
        senderName: String,
        message: String
    ): Result<Unit> = try {
        val notification = Notification(
            userId = receiverId,
            title = "Tin nhắn mới từ $senderName",
            body = message,
            type = NotificationType.CHAT_MESSAGE,
            orderId = orderId,
            timestamp = System.currentTimeMillis(),
            data = mapOf("orderId" to orderId)
        )
        firestore.collection("notifications").add(notification).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error sending chat notification")
        Result.failure(e)
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> = try {
        firestore.collection("notifications").document(notificationId)
            .update("isRead", true).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error marking notification as read")
        Result.failure(e)
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> = try {
        firestore.collection("notifications").document(notificationId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error deleting notification")
        Result.failure(e)
    }
}
