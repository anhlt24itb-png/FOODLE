package com.foddy.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.foddy.app.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
//Phục vụ chức năng Lưu thông tin khi Đăng nhập/Đăng ký thành công
    @Query("SELECT * FROM users LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>
//giao diện tự động cập nhật thời gian thực: nếu tên hoặc ảnh đại diện thay đổ
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
//Phục vụ chức năng Truy vấn/Kiểm tra thông tin tài khoản của người dùng một cách nhanh chóng thông qua Email hoặc ID
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("DELETE FROM users")
    suspend fun clearUser()
//Phục vụ chức năng Bấm nút "Đăng xuất"
    @Query("UPDATE users SET name = :name WHERE email = :email")
    suspend fun updateName(email: String, name: String)
    //Phục vụ chức năng Chỉnh sửa thông tin cá nhân
}
