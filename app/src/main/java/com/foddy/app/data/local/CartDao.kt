package com.foddy.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItemEntity>>
//Phục vụ cho Màn hình Giỏ hàng
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(item: CartItemEntity)
//thêm món ăn vào giỏ
    @Update
    suspend fun updateCartItem(item: CartItemEntity)
//các nút Tăng/Giảm số lượng (Nút + và - trong màn hình giỏ hàng
    @Query("DELETE FROM cart_items WHERE foodId = :foodId")
    suspend fun removeFromCart(foodId: String)
//Bấm nút thùng rác (Xóa món)
    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
//chức năng Xóa sạch giỏ hàng
    @Query("SELECT * FROM cart_items WHERE foodId = :foodId LIMIT 1")
    suspend fun getCartItemById(foodId: String): CartItemEntity?
    //Kiểm tra xem một món ăn cụ thể đã tồn tại trong giỏ hàng hay chưa để hệ thống đưa ra quyết định là tăng số lượng hay thêm mới.
}
