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

        val responseText = try {
            geminiClient.generateText(
                prompt = prompt,
                systemInstruction = PromptManager.getEssayCorrectionPrompt(),
                temperature = temperature,
                maxOutputTokens = 2048,
                modelName = if (modelName.contains("llama") || modelName.isBlank()) null else modelName
            )
        } catch (e: Exception) {
            throw e.toAIError()
        }
        
        if (responseText.isBlank()) {
            throw AIError.Unknown("Resposta vazia da IA ao analisar redação.")
        }
        
        return try {
            // Limpar o JSON (remover blocos de markdown e chaves residuais)
            var cleanJson = responseText.replace("```json", "").replace("```", "").trim()
            val startIndex = cleanJson.indexOf("{")
            val endIndex = cleanJson.lastIndexOf("}")
            if (startIndex != -1 && endIndex != -1) {
                cleanJson = cleanJson.substring(startIndex, endIndex + 1)
            }
            val parsedResult = json.decodeFromString<AICorrectionResult>(cleanJson)

            val c1 = parsedResult.comp1.copy(score = parsedResult.comp1.score.coerceIn(0, 200))
            val c2 = parsedResult.comp2.copy(score = parsedResult.comp2.score.coerceIn(0, 200))
            val c3 = parsedResult.comp3.copy(score = parsedResult.comp3.score.coerceIn(0, 200))
            val c4 = parsedResult.comp4.copy(score = parsedResult.comp4.score.coerceIn(0, 200))
            val c5 = parsedResult.comp5.copy(score = parsedResult.comp5.score.coerceIn(0, 200))
            val calculatedTotal = c1.score + c2.score + c3.score + c4.score + c5.score

            parsedResult.copy(
                comp1 = c1,
                comp2 = c2,
                comp3 = c3,
                comp4 = c4,
                comp5 = c5,
                totalScore = calculatedTotal
            )
        } catch (e: Exception) {
            throw AIError.Unknown("Falha ao interpretar a avaliação estruturada da redação.", e)
        }
    }
}
