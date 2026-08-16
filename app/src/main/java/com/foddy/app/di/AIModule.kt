package com.foddy.app.di

import com.foddy.app.BuildConfig
import com.foddy.app.data.repository.AIRepositoryImpl
import com.foddy.app.domain.repository.AIRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {
//Nơi khởi tạo và cung cấp SDK Google Gemini AI. Nó cấu hình chìa khóa API (API Key)
    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        val config = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 2048
        }

        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = config
        )
    }
}
