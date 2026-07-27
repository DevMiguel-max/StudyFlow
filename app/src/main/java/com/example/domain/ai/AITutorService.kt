package com.example.domain.ai

import com.studyflow.app.BuildConfig

class AITutorService {
    
    suspend fun sendMessage(
        modelName: String,
        temperature: Float,
        maxTokens: Int,
        history: List<Pair<String, Boolean>>, 
        message: String
    ): String {
        val apiKey = BuildConfig.NVIDIA_API_KEY.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY
        
        val messages = mutableListOf<NvidiaMessage>()
        
        // System prompt
        messages.add(NvidiaMessage(role = "system", content = PromptManager.getTutorSystemPrompt()))
        
        // Chat history
        history.forEach { (text, isUser) ->
            val role = if (isUser) "user" else "assistant"
            messages.add(NvidiaMessage(role = role, content = text))
        }
        
        // New message
        messages.add(NvidiaMessage(role = "user", content = message))
        
        val request = NvidiaChatRequest(
            model = modelName,
            messages = messages,
            temperature = temperature,
            max_tokens = maxTokens
        )
        
        return try {
            val response = NvidiaApiClient.api.generateCompletion("Bearer $apiKey", request)
            response.choices.firstOrNull()?.message?.content ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            "Desculpe, ocorreu um erro ao se comunicar com o tutor AI."
        }
    }
}
