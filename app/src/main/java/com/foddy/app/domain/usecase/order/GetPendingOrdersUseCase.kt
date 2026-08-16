package com.foddy.app.domain.usecase.order

import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingOrdersUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    operator fun invoke(): Flow<List<OrderRequest>> {
        return repository.getPendingOrders()
    }
}
