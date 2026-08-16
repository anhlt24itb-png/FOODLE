package com.foddy.app.domain.repository

import com.foddy.app.domain.model.Favorite
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavorites(userId: String): Flow<List<Favorite>>
    suspend fun toggleFavorite(userId: String, targetId: String, type: String, name: String = "", image: String = ""): Result<Unit>
    suspend fun isFavorite(userId: String, targetId: String): Boolean
}
