package com.foddy.app.domain.usecase.menu

import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.Category
import com.foddy.app.domain.repository.MenuRepository
import com.foddy.app.core.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMenuItemsUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    operator fun invoke(restaurantId: String? = null): Flow<List<FoodItem>> = repository.getMenuItems(restaurantId)

    fun executeWithCache(restaurantId: String? = null): Flow<Resource<List<FoodItem>>> = 
        repository.getMenuItemsWithCache(restaurantId)

    fun executeCategories(): Flow<List<Category>> = repository.getCategories()
}
