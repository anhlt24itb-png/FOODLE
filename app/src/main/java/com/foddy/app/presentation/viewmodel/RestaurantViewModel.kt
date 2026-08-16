package com.foddy.app.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.Restaurant
import com.foddy.app.domain.usecase.restaurant.GetRestaurantsUseCase
import com.foddy.app.core.Resource
import com.foddy.app.presentation.ui.state.UiState
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class RestaurantState(
    val restaurants: List<Restaurant> = emptyList(),
    val isLastPage: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false
)

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    private val getRestaurantsUseCase: GetRestaurantsUseCase,
    private val restaurantRepository: com.foddy.app.domain.repository.RestaurantRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<RestaurantState>>(UiState.Loading)
    val uiState: StateFlow<UiState<RestaurantState>> = _uiState.asStateFlow()

    private var allRestaurants = mutableListOf<Restaurant>()
    private var lastVisible: DocumentSnapshot? = null
    private var isLastPage = false
    private var isCurrentlyLoading = false

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    init {
        observeRestaurantsWithCache()
    }

    fun resetActionState() {
        _actionState.value = UiState.Idle
    }
    
    private fun observeRestaurantsWithCache() {
        viewModelScope.launch {
            getRestaurantsUseCase.executeWithCache()
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Unknown Error")
                }
                .collect { resource ->
                    when (resource) {
                    is Resource.Loading -> {
                        if (resource.data.isNullOrEmpty()) {
                            _uiState.value = UiState.Loading
                        } else {
                            _uiState.value = UiState.Success(
                                RestaurantState(
                                    restaurants = resource.data,
                                    isLoadingMore = true
                                )
                            )
                        }
                    }
                    is Resource.Success -> {
                        allRestaurants = resource.data?.toMutableList() ?: mutableListOf()
                        _uiState.value = UiState.Success(
                            RestaurantState(
                                restaurants = allRestaurants,
                                isLastPage = true // For cached full list, we might disable pagination or handle it differently
                            )
                        )
                    }
                    is Resource.Error -> {
                        if (resource.data.isNullOrEmpty()) {
                            _uiState.value = UiState.Error(resource.message ?: "Unknown Error")
                        } else {
                            _uiState.value = UiState.Success(
                                RestaurantState(
                                    restaurants = resource.data,
                                    error = resource.message,
                                    isOffline = true
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadMoreRestaurants() {
        if (isCurrentlyLoading || isLastPage) return

        viewModelScope.launch {
            isCurrentlyLoading = true
            
            val currentState = _uiState.value
            if (currentState is UiState.Success) {
                _uiState.value = UiState.Success(currentState.data.copy(isLoadingMore = true))
            }

            try {
                getRestaurantsUseCase(limit = 10, lastVisible = lastVisible).collect { result ->
                    val newRestaurants = result.first
                    val nextVisible = result.second
                    
                    if (newRestaurants.isEmpty()) {
                        isLastPage = true
                    } else {
                        val currentIds = allRestaurants.map { it.id }.toSet()
                        val filteredNew = newRestaurants.filter { it.id !in currentIds }
                        allRestaurants.addAll(filteredNew)
                        lastVisible = nextVisible
                    }
                    
                    _uiState.value = UiState.Success(
                        RestaurantState(
                            restaurants = allRestaurants.toList(),
                            isLastPage = isLastPage,
                            isLoadingMore = false
                        )
                    )
                    isCurrentlyLoading = false
                }
            } catch (e: Exception) {
                if (allRestaurants.isEmpty()) {
                    _uiState.value = UiState.Error(e.message ?: "Unknown Error")
                } else {
                    _uiState.value = UiState.Success(
                        RestaurantState(
                            restaurants = allRestaurants.toList(),
                            isLastPage = isLastPage,
                            isLoadingMore = false,
                            error = e.message
                        )
                    )
                }
                isCurrentlyLoading = false
            }
        }
    }

    fun refresh() {
        lastVisible = null
        isLastPage = false
        allRestaurants.clear()
        _uiState.value = UiState.Loading
        loadMoreRestaurants()
    }

    suspend fun getRestaurantById(id: String): Restaurant? {
        return try {
            getRestaurantsUseCase.getById(id)
        } catch (e: Exception) {
            null
        }
    }

    fun updateRestaurant(restaurant: Restaurant) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            restaurantRepository.updateRestaurant(restaurant)
                .onSuccess {
                    _actionState.value = UiState.Success("Cập nhật thông tin nhà hàng thành công")
                }
                .onFailure { e ->
                    _actionState.value = UiState.Error(e.message ?: "Lỗi khi cập nhật thông tin")
                }
        }
    }
}
