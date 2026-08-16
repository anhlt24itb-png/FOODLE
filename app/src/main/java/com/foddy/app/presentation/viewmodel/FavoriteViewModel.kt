package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.Favorite
import com.foddy.app.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val repository: FavoriteRepository
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites = _favorites.asStateFlow()

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            repository.getFavorites(userId).collect { _favorites.value = it }
        }
    }

    fun toggleFavorite(userId: String, targetId: String, type: String, name: String = "", image: String = "") {
        viewModelScope.launch { repository.toggleFavorite(userId, targetId, type, name, image) }
    }
}
