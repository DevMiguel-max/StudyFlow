package com.example.domain.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.studyflow.app.BuildConfig

class AITutorService {
    
    private fun getModel(modelName: String, temperature: Float, maxTokens: Int): GenerativeModel {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                this.temperature = temperature
                this.maxOutputTokens = maxTokens
            },
            systemInstruction = content { text(PromptManager.getTutorSystemPrompt()) }
        )
    }

    suspend fun sendMessage(
        modelName: String,
        temperature: Float,
        maxTokens: Int,
        history: List<Pair<String, Boolean>>, 
        message: String
    ): String {
        val model = getModel(modelName, temperature, maxTokens)
        val chatHistory = history.map { (text, isUser) ->
            content(if (isUser) "user" else "model") { text(text) }
        }
        val chat = model.startChat(chatHistory)
        val response = chat.sendMessage(message)
        return response.text ?: ""
    }
}
