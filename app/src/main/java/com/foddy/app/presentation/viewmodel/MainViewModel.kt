package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.usecase.post.GetPostsUseCase
import com.foddy.app.presentation.state.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        fetchPosts()
    }

    fun fetchPosts(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefreshing, isRefreshing = isRefreshing) }
            getPostsUseCase().collect { result ->
                result.onSuccess { posts ->
                    _uiState.update { 
                        it.copy(
                            posts = posts, 
                            isLoading = false, 
                            isRefreshing = false,
                            error = null
                        ) 
                    }
                }.onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            isRefreshing = false, 
                            error = error.message ?: "Unknown error"
                        ) 
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
