package com.foddy.app.domain.usecase.order

import com.foddy.app.domain.repository.OrderRepository
import javax.inject.Inject

class UpdateOrderStatusUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, status: String) {
        repository.updateOrderStatus(orderId, status)
    }
}
