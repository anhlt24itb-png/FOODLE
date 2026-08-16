package com.foddy.app.domain.usecase.ai

import com.foddy.app.BuildConfig
import com.foddy.app.domain.model.FoodItem
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject

class GetAIChatResponseUseCase @Inject constructor() {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend operator fun invoke(text: String, allItems: List<FoodItem>): String? {
        val prompt = """
            Bạn là một trợ lý ảo của ứng dụng Foddy, chuyên gợi ý món ăn.
            Người dùng hỏi: "$text".
            Danh sách món ăn có sẵn: ${allItems.joinToString { it.name }}.
            Hãy trả lời thân thiện và gợi ý các món phù hợp từ danh sách trên nếu có thể.
        """.trimIndent()

        val response = generativeModel.generateContent(prompt)
        return response.text
    }
}
