package com.foddy.app.domain.usecase.post

import com.foddy.app.domain.model.Post
import com.foddy.app.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(): Flow<Result<List<Post>>> {
        return repository.getPosts()
    }
}
