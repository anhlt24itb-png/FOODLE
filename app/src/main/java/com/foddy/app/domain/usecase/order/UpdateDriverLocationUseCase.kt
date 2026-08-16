package com.foddy.app.domain.usecase.order

import com.foddy.app.domain.repository.OrderRepository
import javax.inject.Inject

class UpdateDriverLocationUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, lat: Double, lng: Double) {
        repository.updateDriverLocation(orderId, lat, lng)
    }
}
