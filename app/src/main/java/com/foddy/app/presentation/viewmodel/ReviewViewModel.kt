package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.Review
import com.foddy.app.domain.repository.ReviewRepository
import com.foddy.app.presentation.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: ReviewRepository
) : ViewModel() {

    private val _reviews = MutableStateFlow<UiState<List<Review>>>(UiState.Idle)
    val reviews = _reviews.asStateFlow()

    fun loadReviews(targetId: String) {
        viewModelScope.launch {
            _reviews.value = UiState.Loading
            repository.getReviewsByTarget(targetId)
                .catch { _reviews.value = UiState.Error(it.message ?: "Error") }
                .collect { _reviews.value = UiState.Success(it) }
        }
    }

    fun addReview(review: Review) = viewModelScope.launch { repository.addReview(review) }
}
