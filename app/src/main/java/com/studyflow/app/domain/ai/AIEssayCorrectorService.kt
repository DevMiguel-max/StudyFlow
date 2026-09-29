package com.studyflow.app.domain.ai

import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import com.studyflow.app.BuildConfig

class AIEssayCorrectorService {
    
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun correctEssay(
        modelName: String,
        temperature: Float,
        essayText: String,
        themeTitle: String,
        motivationalTexts: String
    ): AICorrectionResult {
        // Obter chave da API do NVIDIA NIM (usando a mesma variável por conveniência, ou NVIDIA_API_KEY se configurada)
        val apiKey = BuildConfig.NVIDIA_API_KEY
        
        val prompt = """
            Tema: $themeTitle
            
            Textos Motivadores:
            $motivationalTexts
            
            Redação do Aluno:
            $essayText
            
            Avalie a redação acima seguindo as diretrizes do ENEM. Retorne apenas o JSON.
        """.trimIndent()
        
        val request = NvidiaChatRequest(
            model = modelName, // ex: "meta/llama3-70b-instruct"
            temperature = temperature,
            messages = listOf(
                NvidiaMessage(role = "system", content = PromptManager.getEssayCorrectionPrompt()),
                NvidiaMessage(role = "user", content = prompt)
            ),
            max_tokens = 2048
        )

        val response = NvidiaApiClient.api.generateCompletion("Bearer $apiKey", request)
        
        val responseText = response.choices.firstOrNull()?.message?.content ?: throw Exception("Resposta vazia da IA da NVIDIA")
        
        // Limpar o JSON (remover blocos de markdown e chaves residuais)
        var cleanJson = responseText.replace("```json", "").replace("```", "").trim()
        val startIndex = cleanJson.indexOf("{")
        val endIndex = cleanJson.lastIndexOf("}")
        if (startIndex != -1 && endIndex != -1) {
            cleanJson = cleanJson.substring(startIndex, endIndex + 1)
        }
        
        return json.decodeFromString(cleanJson)
    }
}
