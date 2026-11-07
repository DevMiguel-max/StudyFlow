package com.studyflow.app.domain.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withTimeout

class GeminiClient(
    val defaultModelName: String = AIConfig.MODEL
) {
    companion object {
        private const val TAG = "GeminiClient"
    }

    private val ai by lazy { Firebase.ai(backend = GenerativeBackend.googleAI()) }

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

            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            response.text ?: ""
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Exceção capturada ao gerar conteúdo com o modelo $targetModel", e)
            throw e.toAIError()
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
                if (e is CancellationException) throw e
                Log.e(TAG, "Exceção capturada no stream do modelo $targetModel", e)
                throw e.toAIError()
            }
    }
}
