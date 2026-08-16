package com.foddy.app.domain.usecase.notification

import com.foddy.app.domain.repository.NotificationRepository
import com.foddy.app.domain.repository.OrderEvent
import javax.inject.Inject

class SendOrderStatusNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(userId: String, orderId: String, event: OrderEvent) =
        repository.sendOrderStatusNotification(userId, orderId, event)
}
