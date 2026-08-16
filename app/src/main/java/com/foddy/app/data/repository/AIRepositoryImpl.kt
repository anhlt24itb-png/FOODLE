package com.foddy.app.data.repository

import com.foddy.app.domain.model.*
import com.foddy.app.domain.repository.AIRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class AIRepositoryImpl @Inject constructor(
    private val generativeModel: GenerativeModel
) : AIRepository {

    private val gson = Gson()

    override suspend fun getChatResponse(history: List<ChatMessage>, prompt: String): Flow<String> = flow {
        try {
            val chatHistory = history.map { 
                content(role = if (it.role == MessageRole.USER) "user" else "model") { text(it.content) }
            }
            val chat = generativeModel.startChat(chatHistory)
            chat.sendMessageStream(prompt).collect { response ->
                response.text?.let { emit(it) }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting chat response")
            emit("Xin lỗi, tôi gặp sự cố khi kết nối. Vui lòng thử lại sau.")
        }
    }

    override suspend fun getFoodRecommendations(
        userPreferences: String,
        currentWeather: String,
        currentTime: String,
        availableMenu: List<FoodItem>
    ): Result<List<AIRecommendation>> = runCatching {
        val menuJson = gson.toJson(availableMenu.take(20))
        val prompt = """
            Dựa trên sở thích: $userPreferences, thời tiết: $currentWeather, thời gian: $currentTime.
            Hãy chọn ra 3 món ăn từ menu dưới đây phù hợp nhất:
            $menuJson
            
            Trả về kết quả dưới dạng JSON Array, mỗi Object có: 
            - title (tên món)
            - reason (lý do gợi ý ngắn gọn, thuyết phục)
            - type (PERSONALIZED, TIME_BASED, WEATHER_BASED, BEHAVIOR_BASED)
        """.trimIndent()

        val response = generativeModel.generateContent(prompt)
        val jsonString = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: ""
        val listType = object : TypeToken<List<AIRecommendation>>() {}.type
        val recommendations: List<AIRecommendation> = gson.fromJson(jsonString, listType)
        
        // Map back to full FoodItem objects if needed
        recommendations.map { rec ->
            rec.copy(foodItems = availableMenu.filter { it.name.contains(rec.title, ignoreCase = true) })
        }
    }

    override suspend fun semanticSearch(query: String, menu: List<FoodItem>): Result<List<FoodItem>> = runCatching {
        val menuNames = menu.map { it.name }.joinToString(", ")
        val prompt = "Trong các món sau: $menuNames. Món nào phù hợp nhất với yêu cầu: '$query'? Trả về danh sách tên món, ngăn cách bởi dấu phẩy."
        
        val response = generativeModel.generateContent(prompt)
        val suggestedNames = response.text?.split(",")?.map { it.trim() } ?: emptyList()
        
        menu.filter { item ->
            suggestedNames.any { it.contains(item.name, ignoreCase = true) || item.name.contains(it, ignoreCase = true) }
        }
    }

    override suspend fun analyzeReviews(reviews: List<String>): Result<ReviewSummary> = runCatching {
        val prompt = "Tóm tắt các đánh giá sau đây thành: ưu điểm, nhược điểm, và nhận định chung. Trả về JSON: {positiveSummary, negativeSummary, overallInsight, rating (1-5)} \n Reviews: ${reviews.joinToString("\n")}"
        
        val response = generativeModel.generateContent(prompt)
        val jsonString = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: ""
        gson.fromJson(jsonString, ReviewSummary::class.java)
    }

    override suspend fun getBusinessInsights(orderData: String): Result<String> = runCatching {
        val prompt = "Phân tích dữ liệu đơn hàng sau và đưa ra dự báo kinh doanh, xu hướng doanh thu và thời điểm đông khách nhất: $orderData"
        val response = generativeModel.generateContent(prompt)
        response.text ?: "Không có dữ liệu phân tích."
    }
}
