package com.foddy.app.domain.repository

import com.foddy.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AIRepository {
    // Chat & Support
    suspend fun getChatResponse(history: List<ChatMessage>, prompt: String): Flow<String>
    
    // Recommendations
    suspend fun getFoodRecommendations(
        userPreferences: String,
        currentWeather: String,
        currentTime: String,
        availableMenu: List<FoodItem>
    ): Result<List<AIRecommendation>>
    
    // Search
    suspend fun semanticSearch(query: String, menu: List<FoodItem>): Result<List<FoodItem>>
    
    // Analytics
    suspend fun analyzeReviews(reviews: List<String>): Result<ReviewSummary>
    
    // Admin Insights
    suspend fun getBusinessInsights(orderData: String): Result<String>
}
