package com.foddy.app.domain.repository

import com.foddy.app.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPosts(): Flow<Result<List<Post>>>
}
