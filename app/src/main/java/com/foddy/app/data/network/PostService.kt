package com.foddy.app.data.network

import com.foddy.app.domain.model.Post
import retrofit2.http.GET

interface PostService {
    @GET("posts")
    suspend fun getPosts(): List<Post>
}