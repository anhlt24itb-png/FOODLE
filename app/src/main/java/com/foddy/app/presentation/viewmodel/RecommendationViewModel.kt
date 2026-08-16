package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.AIRecommendation
import com.foddy.app.domain.model.RecommendationType
import com.foddy.app.domain.repository.AIRepository
import com.foddy.app.domain.repository.MenuRepository
import com.foddy.app.core.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val aiRepository: AIRepository,
    private val menuRepository: MenuRepository
) : ViewModel() {
    private val _recommendations = MutableStateFlow<List<FoodItem>>(emptyList())
    val recommendations: StateFlow<List<FoodItem>> = _recommendations.asStateFlow()

    private val _aiRecs = MutableStateFlow<List<AIRecommendation>>(emptyList())
    val aiRecs: StateFlow<List<AIRecommendation>> = _aiRecs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchRecommendations(userPreferences: String) {
        viewModelScope.launch {
            _isLoading.value = true
            menuRepository.getMenuItemsWithCache().first { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    val menu = resource.data
                    if (menu.isEmpty()) return@first true
                    
                    val result = aiRepository.getFoodRecommendations(
                        userPreferences = userPreferences,
                        currentWeather = "Nắng nóng 35 độ", 
                        currentTime = "12:00 PM",
                        availableMenu = menu
                    )
                    
                    result.onSuccess { recs ->
                        _aiRecs.value = recs
                        _recommendations.value = recs.flatMap { it.foodItems }.distinctBy { it.id }
                    }
                    true
                } else if (resource is Resource.Error) {
                    true
                } else {
                    false
                }
            }
            _isLoading.value = false
        }
    }
}
