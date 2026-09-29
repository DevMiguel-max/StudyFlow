package com.studyflow.app.domain.ai

import com.studyflow.app.data.local.GeneratedQuestion
import com.studyflow.app.data.local.Flashcard
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MaterialGeneratorService(
    private val geminiClient: GeminiClient = GeminiClient()
) {
    
    suspend fun generateMaterial(sourceText: String, type: String, options: Map<String, String> = emptyMap()): String {
        val cacheKey = "material_${type}_${sourceText.hashCode()}"
        return AIHelper.withRetryAndTelemetry("generateMaterial", cacheKey = cacheKey) {
            val systemPrompt = when (type) {
                "SUMMARY_QUICK" -> "Crie um resumo rápido (máximo 150 palavras) do texto a seguir, focando apenas nos pontos cruciais."
                "SUMMARY_COMPLETE" -> "Crie um resumo completo do texto a seguir, cobrindo todos os detalhes, conceitos e definições importantes. Mantenha a estrutura em parágrafos e tópicos quando aplicável."
                "SUMMARY_TOPIC" -> "Crie um resumo por tópicos (bullet points) do texto a seguir, destacando a hierarquia da informação."
                "SUMMARY_CHRONO" -> "Crie um resumo cronológico do texto a seguir, organizando eventos, passos ou processos em ordem temporal de forma clara e estruturada."
                "SUMMARY_REVIEW" -> "Crie um resumo para revisão rápida, extraindo fórmulas, datas, nomes e palavras-chave principais do texto a seguir."
                "SUMMARY_CONTEST" -> "Crie um resumo focado em concursos públicos para o texto a seguir. Extraia o que tem maior probabilidade de ser cobrado (leis, prazos, definições absolutas, exceções)."
                "SUMMARY_ENEM" -> "Crie um resumo focado no formato ENEM (interdisciplinar, focado em causas/consequências e aplicação prática) para o texto a seguir."
                "SUMMARY_VESTIBULAR" -> "Crie um resumo focado em vestibulares tradicionais (focado em conceitos exatos, características e classificações rígidas) para o texto a seguir."
                "MIND_MAP" -> "Gere uma estrutura de mapa mental em texto baseada no documento, usando indentação com hífens (ex: Matéria -> Tema -> Subtema -> Conceitos -> Exemplos)."
                else -> "Resuma o texto a seguir."
            }
            
            val language = options["language"] ?: "português"
            val fullPrompt = "$systemPrompt\n\nResponda em $language.\n\nTexto original:\n${sourceText.take(15000)}"

            val response = geminiClient.generateText(
                prompt = fullPrompt,
                systemInstruction = systemPrompt
            )
            response.ifBlank { "Não foi possível gerar o material." }
        }
    }

    suspend fun generateFlashcards(sourceText: String, count: Int, difficulty: String, subjectId: Int, subjectName: String): List<Flashcard> {
        val cacheKey = "flashcards_${difficulty}_${sourceText.hashCode()}"
        return AIHelper.withRetryAndTelemetry("generateFlashcards") {
            val systemPrompt = """
                Gere $count flashcards baseados no texto fornecido, com nível de dificuldade '$difficulty'.
                Retorne APENAS um array JSON válido. Formato:
                [
                  {
                    "question": "Pergunta do flashcard",
                    "answer": "Resposta do flashcard",
                    "category": "Categoria/Tema principal",
                    "topic": "Tópico específico"
                  }
                ]
                Não adicione crases Markdown (` ```json `), responda apenas com o JSON cru.
            """.trimIndent()

            val responseText = geminiClient.generateText(
                prompt = "Texto:\n${sourceText.take(15000)}",
                systemInstruction = systemPrompt,
                temperature = 0.3f
            )
            val jsonText = responseText.ifBlank { "[]" }
            
            val cleanJson = jsonText.substringAfter("[").substringBeforeLast("]")
            val finalJsonStr = "[$cleanJson]"
            
            val jsonArray = Json.parseToJsonElement(finalJsonStr).jsonArray
            jsonArray.map { element ->
                val obj = element.jsonObject
                Flashcard(
                    subjectId = subjectId,
                    question = obj["question"]?.jsonPrimitive?.content ?: "",
                    answer = obj["answer"]?.jsonPrimitive?.content ?: "",
                    category = obj["category"]?.jsonPrimitive?.content,
                    topic = obj["topic"]?.jsonPrimitive?.content,
                    difficulty = difficulty,
                    source = "Gerado via IA",
                    nextReviewDate = System.currentTimeMillis()
                )
            }
        }
    }

    suspend fun generateQuestions(sourceText: String, count: Int, difficulty: String, subjectId: Int, subjectName: String): List<GeneratedQuestion> {
        return AIHelper.withRetryAndTelemetry("generateQuestions") {
            val systemPrompt = """
                Gere $count questões de múltipla escolha baseadas no texto, nível de dificuldade '$difficulty'.
                Cada questão deve ter 5 alternativas (de A a E).
                Retorne APENAS um array JSON válido no formato:
                [
                  {
                    "text": "Enunciado da questão",
                    "options": ["Opção A", "Opção B", "Opção C", "Opção D", "Opção E"],
                    "correctAnswerIndex": 0, // 0 a 4
                    "explanation": "Explicação da resposta",
                    "topic": "Tópico específico"
                  }
                ]
                Não adicione crases Markdown (` ```json `), responda apenas com o JSON cru.
            """.trimIndent()

            val responseText = geminiClient.generateText(
                prompt = "Texto:\n${sourceText.take(15000)}",
                systemInstruction = systemPrompt,
                temperature = 0.3f
            )
            val jsonText = responseText.ifBlank { "[]" }
            
            val cleanJson = jsonText.substringAfter("[").substringBeforeLast("]")
            val finalJsonStr = "[$cleanJson]"
            
            val jsonArray = Json.parseToJsonElement(finalJsonStr).jsonArray
            jsonArray.map { element ->
                val obj = element.jsonObject
                val optionsList = obj["options"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                val optionsStr = kotlinx.serialization.json.JsonArray(optionsList.map { kotlinx.serialization.json.JsonPrimitive(it) }).toString()
                
                val cIndex = obj["correctAnswerIndex"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                val correctAns = optionsList.getOrNull(cIndex) ?: ""

                GeneratedQuestion(
                    type = "MULTIPLE_CHOICE",
                    statement = obj["text"]?.jsonPrimitive?.content ?: "",
                    options = optionsStr,
                    correctAnswer = correctAns,
                    explanation = obj["explanation"]?.jsonPrimitive?.content ?: "",
                    difficulty = difficulty,
                    subject = subjectName,
                    topic = obj["topic"]?.jsonPrimitive?.content ?: "",
                    source = "Gerado via IA"
                )
            }
        }
    }
}
