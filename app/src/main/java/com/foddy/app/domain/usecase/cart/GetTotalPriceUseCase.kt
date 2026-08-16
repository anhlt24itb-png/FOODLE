package com.foddy.app.domain.usecase.cart

import com.foddy.app.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTotalPriceUseCase @Inject constructor(
    private val repository: CartRepository
) {
    operator fun invoke(): Flow<Double> = repository.getTotalPrice()
}
