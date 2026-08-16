package com.foddy.app.presentation.state

import com.foddy.app.domain.model.Post

data class MainUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val searchQuery: String = ""
)
