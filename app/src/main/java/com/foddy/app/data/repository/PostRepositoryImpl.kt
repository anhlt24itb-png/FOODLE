package com.foddy.app.data.repository

import com.foddy.app.data.network.PostService
import com.foddy.app.domain.model.Post
import com.foddy.app.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postService: PostService
) : PostRepository {
    override fun getPosts(): Flow<Result<List<Post>>> = flow {
        try {
            val posts = postService.getPosts()
            emit(Result.success(posts))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
