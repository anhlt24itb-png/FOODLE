package com.foddy.app.domain.usecase.ai

import com.foddy.app.BuildConfig
import com.foddy.app.domain.model.FoodItem
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject

class GetAIRecommendationsUseCase @Inject constructor() {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend operator fun invoke(userPreferences: String, allItems: List<FoodItem>): List<String> {
        val prompt = """
            Dựa trên sở thích của người dùng: "$userPreferences".
            Hãy chọn ra tối đa 3 món ăn phù hợp nhất từ danh sách sau:
            ${allItems.joinToString { it.name }}
            Trả về CHỈ tên các món ăn, cách nhau bởi dấu phẩy.
        """.trimIndent()

        val response = generativeModel.generateContent(prompt)
        return response.text?.split(",")?.map { it.trim() } ?: emptyList()
    }
}
