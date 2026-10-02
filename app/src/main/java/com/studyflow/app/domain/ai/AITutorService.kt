package com.studyflow.app.domain.ai

class AITutorService(
    private val geminiClient: GeminiClient = GeminiClient()
) {
    
    suspend fun sendMessage(
        modelName: String,
        temperature: Float,
        maxTokens: Int,
        history: List<Pair<String, Boolean>>, 
        message: String
    ): String {
        val systemPrompt = PromptManager.getTutorSystemPrompt()
        
        val conversationHistory = buildString {
            if (history.isNotEmpty()) {
                append("Histórico da conversa:\n")
                history.forEach { (text, isUser) ->
                    val role = if (isUser) "Estudante" else "Tutor"
                    append("$role: $text\n")
                }
                append("\n")
            }
            append("Mensagem do estudante: $message")
        }
        
        val response = try {
            geminiClient.generateText(
                prompt = conversationHistory,
                systemInstruction = systemPrompt,
                temperature = temperature,
                maxOutputTokens = maxTokens,
                modelName = if (modelName.contains("llama") || modelName.isBlank()) null else modelName
            )
        } catch (e: Exception) {
            throw e.toAIError()
        }

        if (response.isBlank()) {
            throw AIError.Unknown("Não foi possível obter uma resposta do tutor AI.")
        }
        return response
    }
}
