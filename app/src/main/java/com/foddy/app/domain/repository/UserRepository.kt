package com.foddy.app.domain.repository

import com.foddy.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    fun isInitialized(): Flow<Boolean>
    suspend fun register(name: String, email: String, password: String, role: String): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun updateName(name: String): Result<Unit>
    suspend fun updateAvatar(imageUrl: String): Result<Unit>
    suspend fun updatePhone(phone: String): Result<Unit>
    suspend fun updateFcmToken(token: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun logout()
}
