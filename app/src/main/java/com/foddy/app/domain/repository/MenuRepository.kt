package com.foddy.app.domain.repository

import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.Category
import com.foddy.app.core.Resource
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    fun getMenuItems(restaurantId: String? = null): Flow<List<FoodItem>>
    suspend fun addMenuItem(item: FoodItem)
    suspend fun updateMenuItem(item: FoodItem)
    suspend fun removeMenuItem(item: FoodItem)
    fun getMenuItemsWithCache(restaurantId: String? = null): Flow<Resource<List<FoodItem>>>
    
    // Category management
    fun getCategories(): Flow<List<Category>>
    suspend fun addCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(categoryId: String)
}
