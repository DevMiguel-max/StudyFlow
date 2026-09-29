package com.studyflow.app.domain.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GeminiClient(
    val defaultModelName: String = "gemini-2.5-flash"
) {
    private val ai = Firebase.ai(backend = GenerativeBackend.googleAI())

    suspend fun generateText(
        prompt: String,
        systemInstruction: String? = null,
        temperature: Float? = null,
        maxOutputTokens: Int? = null,
        modelName: String? = null
    ): String {
        val targetModel = modelName?.takeIf { it.isNotBlank() } ?: defaultModelName
        
        val config = if (temperature != null || maxOutputTokens != null) {
            generationConfig {
                temperature?.let { this.temperature = it }
                maxOutputTokens?.let { this.maxOutputTokens = it }
            }
        } else null

        val sysContent = systemInstruction?.let {
            content { text(it) }
        }

        val model = ai.generativeModel(
            modelName = targetModel,
            generationConfig = config,
            systemInstruction = sysContent
        )

        val response = model.generateContent(prompt)
        return response.text ?: ""
    }

    fun generateContentStream(
        prompt: String,
        modelName: String? = null
    ): Flow<String> {
        val targetModel = modelName?.takeIf { it.isNotBlank() } ?: defaultModelName
        val model = ai.generativeModel(targetModel)
        return model.generateContentStream(prompt).map { it.text ?: "" }
    }
}
