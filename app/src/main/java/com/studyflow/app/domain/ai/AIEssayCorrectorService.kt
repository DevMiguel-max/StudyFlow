package com.studyflow.app.domain.ai

import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class AIEssayCorrectorService(
    private val geminiClient: GeminiClient = GeminiClient()
) {
    
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun correctEssay(
        modelName: String,
        temperature: Float,
        essayText: String,
        themeTitle: String,
        motivationalTexts: String
    ): AICorrectionResult {
        val prompt = """
            Tema: $themeTitle
            
            Textos Motivadores:
            $motivationalTexts
            
            Redação do Aluno:
            $essayText
            
            Avalie a redação acima seguindo as diretrizes do ENEM. Retorne apenas o JSON.
        """.trimIndent()

        val responseText = geminiClient.generateText(
            prompt = prompt,
            systemInstruction = PromptManager.getEssayCorrectionPrompt(),
            temperature = temperature,
            maxOutputTokens = 2048,
            modelName = if (modelName.contains("llama") || modelName.isBlank()) null else modelName
        )
        
        if (responseText.isBlank()) {
            throw Exception("Resposta vazia da IA")
        }
        
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
