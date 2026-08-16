package com.foddy.app.domain.usecase.order

import com.foddy.app.domain.model.OrderRequest
import com.foddy.app.domain.repository.OrderRepository
import java.util.UUID
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(order: OrderRequest): Result<String> {
        return repository.placeOrder(order)
    }
}
