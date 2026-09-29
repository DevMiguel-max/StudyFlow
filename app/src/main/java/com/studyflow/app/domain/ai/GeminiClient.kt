package com.studyflow.app.domain.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

class GeminiClient(
    val defaultModelName: String = AIConfig.MODEL
) {
    companion object {
        private const val TAG = "GeminiClient"
    }

    private val ai = Firebase.ai(backend = GenerativeBackend.googleAI())

    suspend fun generateText(
        prompt: String,
        systemInstruction: String? = null,
        temperature: Float? = null,
        maxOutputTokens: Int? = null,
        modelName: String? = null
    ): String {
        val targetModel = modelName?.takeIf { it.isNotBlank() } ?: defaultModelName
        
        return try {
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
            response.text ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Exceção capturada ao gerar conteúdo com o modelo $targetModel", e)
            val errorMsg = e.message.orEmpty()
            if (errorMsg.contains("404") || errorMsg.contains("NOT_FOUND", ignoreCase = true) || errorMsg.contains("not found", ignoreCase = true)) {
                "modelo indisponível"
            } else if (errorMsg.contains("403") || errorMsg.contains("PERMISSION_DENIED", ignoreCase = true) || errorMsg.contains("permission denied", ignoreCase = true)) {
                "App Check ou API não habilitados"
            } else {
                throw e
            }
        }
    }

    fun generateContentStream(
        prompt: String,
        modelName: String? = null
    ): Flow<String> {
        val targetModel = modelName?.takeIf { it.isNotBlank() } ?: defaultModelName
        val model = ai.generativeModel(targetModel)
        return model.generateContentStream(prompt)
            .map { it.text ?: "" }
            .catch { e ->
                Log.e(TAG, "Exceção capturada no stream do modelo $targetModel", e)
                val errorMsg = e.message.orEmpty()
                if (errorMsg.contains("404") || errorMsg.contains("NOT_FOUND", ignoreCase = true) || errorMsg.contains("not found", ignoreCase = true)) {
                    emit("modelo indisponível")
                } else if (errorMsg.contains("403") || errorMsg.contains("PERMISSION_DENIED", ignoreCase = true) || errorMsg.contains("permission denied", ignoreCase = true)) {
                    emit("App Check ou API não habilitados")
                } else {
                    throw e
                }
            }
    }
}
