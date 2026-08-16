package com.foddy.app.domain.usecase.restaurant

import com.foddy.app.domain.model.Restaurant
import com.foddy.app.domain.repository.RestaurantRepository
import com.foddy.app.core.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRestaurantsUseCase @Inject constructor(
    private val repository: RestaurantRepository
) {
    operator fun invoke(limit: Long, lastVisible: DocumentSnapshot?): Flow<Pair<List<Restaurant>, DocumentSnapshot?>> =
        repository.getRestaurants(limit, lastVisible)

    suspend fun getById(id: String): Restaurant? = repository.getRestaurantById(id)

    fun executeWithCache(): Flow<Resource<List<Restaurant>>> = repository.getRestaurantsWithCache()
}
