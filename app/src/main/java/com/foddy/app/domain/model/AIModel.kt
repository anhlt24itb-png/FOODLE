package com.foddy.app.domain.model

data class ChatMessage(
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageRole {
    USER, MODEL
}

data class AIRecommendation(
    val title: String,
    val reason: String,
    val foodItems: List<FoodItem> = emptyList(),
    val type: RecommendationType
)

enum class RecommendationType {
    PERSONALIZED, TIME_BASED, WEATHER_BASED, BEHAVIOR_BASED
}

data class ReviewSummary(
    val positiveSummary: String,
    val negativeSummary: String,
    val overallInsight: String,
    val rating: Double
)
