package com.example.domain.ai

import com.studyflow.app.BuildConfig

class DocumentAnalyzerService {
    
    suspend fun analyzeDocument(text: String, analysisType: String): String {
        val cacheKey = "doc_analysis_${analysisType}_${text.hashCode()}"
        return AIHelper.withRetryAndTelemetry("analyzeDocument_$analysisType", cacheKey = cacheKey) {
            val apiKey = BuildConfig.NVIDIA_API_KEY.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY
            val systemPrompt = when (analysisType) {
                "IDENTIFY_TYPE" -> "Você é um assistente especializado em análise de documentos. Identifique o tipo do documento (Edital, Livro, Apostila, Resumo, Lista de exercícios, etc.). Responda apenas com o tipo."
                "SUMMARY_SHORT" -> "Faça um resumo curto (máximo 3 parágrafos) do seguinte documento."
                "SUMMARY_MEDIUM" -> "Faça um resumo médio com os principais pontos do seguinte documento."
                "SUMMARY_COMPLETE" -> "Faça um resumo completo e detalhado do seguinte documento, preservando tópicos principais."
                "ANALYSIS" -> "Analise o seguinte documento e extraia: principais tópicos, conceitos importantes, palavras-chave, fórmulas (se houver) e datas importantes. Formate como Markdown estruturado."
                "EDITAL_ANALYSIS" -> "Este é um edital de concurso. Extraia e formate em Markdown: Cargo, Banca organizadora, Disciplinas cobradas, Conteúdo programático, Datas importantes, Critérios de avaliação, Número de vagas, Requisitos e Etapas do concurso."
                "PLAN_GENERATION" -> "Com base no conteúdo deste edital, gere uma sugestão de plano de estudos semanal com disciplinas, cronograma, metas e revisões. Formate em Markdown estruturado."
                "MIND_MAP" -> "Gere uma estrutura hierárquica em texto baseada no documento para futura conversão em mapa mental. Use indentação com hífens."
                else -> "Analise o seguinte documento."
            }
            
            val request = NvidiaChatRequest(
                model = "meta/llama-3.1-405b-instruct",
                messages = listOf(
                    NvidiaMessage(role = "system", content = systemPrompt),
                    NvidiaMessage(role = "user", content = text.take(15000))
                )
            )

            try {
                val response = NvidiaApiClient.api.generateCompletion("Bearer $apiKey", request)
                response.choices.firstOrNull()?.message?.content ?: "Não foi possível gerar a análise."
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    suspend fun chatWithDocument(documentText: String, question: String): String {
        return AIHelper.withRetryAndTelemetry("chatWithDocument") {
            val apiKey = BuildConfig.NVIDIA_API_KEY.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY
            val systemPrompt = "Você é um assistente prestativo. Use o documento fornecido a seguir como base para responder à pergunta do usuário.\n\nDocumento:\n${documentText.take(15000)}"
            
            val request = NvidiaChatRequest(
                model = "meta/llama-3.1-405b-instruct",
                messages = listOf(
                    NvidiaMessage(role = "system", content = systemPrompt),
                    NvidiaMessage(role = "user", content = question)
                )
            )

            try {
                val response = NvidiaApiClient.api.generateCompletion("Bearer $apiKey", request)
                response.choices.firstOrNull()?.message?.content ?: "Não foi possível responder à pergunta."
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }
}
