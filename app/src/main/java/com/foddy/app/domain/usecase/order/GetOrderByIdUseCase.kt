package com.foddy.app.domain.usecase.order

import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOrderByIdUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    operator fun invoke(orderId: String): Flow<OrderRequest?> {
        return repository.getOrderById(orderId)
    }
}
