package com.foddy.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FoodItemEntity>)

    @Query("SELECT * FROM food_items")
    fun getAllItems(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE restaurantId = :restaurantId")
    fun getItemsByRestaurant(restaurantId: String): Flow<List<FoodItemEntity>>

    @Query("DELETE FROM food_items")
    suspend fun clearAll()
}
