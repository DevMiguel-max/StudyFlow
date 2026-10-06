package com.studyflow.app.domain.ai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EditalSubjectInfo(
    val nome: String,
    val conteudoProgramatico: String = "",
    val assuntos: List<String> = emptyList()
)

@Serializable
data class EditalProgramResult(
    val cargo: String = "",
    val banca: String = "",
    val disciplinas: List<EditalSubjectInfo> = emptyList()
)

class DocumentAnalyzerService(
    private val geminiClient: GeminiClient = GeminiClient()
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    
    suspend fun analyzeDocument(text: String, analysisType: String): String {
        val cacheKey = "doc_analysis_${analysisType}_${text.hashCode()}"
        return AIHelper.withRetryAndTelemetry("analyzeDocument_$analysisType", cacheKey = cacheKey) {
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

            val response = try {
                geminiClient.generateText(
                    prompt = text.take(25000),
                    systemInstruction = systemPrompt
                )
            } catch (e: Exception) {
                throw e.toAIError()
            }

            if (response.isBlank()) {
                throw AIError.Unknown("Não foi possível gerar a análise do documento.")
            }
            response
        }
    }

    suspend fun analyzeEditalProgram(text: String): Pair<EditalProgramResult, Boolean> {
        val maxChunks = 6
        val chunkSize = 25_000
        val totalLength = text.length
        val isTruncated = totalLength > maxChunks * chunkSize
        val chunks = text.chunked(chunkSize).take(maxChunks)

        val results = mutableListOf<EditalProgramResult>()
        for (chunk in chunks) {
            try {
                val chunkResult = analyzeSingleEditalChunk(chunk)
                if (chunkResult.disciplinas.isNotEmpty()) {
                    results.add(chunkResult)
                }
            } catch (e: Exception) {
                // Se algum bloco falhar, continua com os blocos restantes
            }
        }

        if (results.isEmpty()) {
            throw AIError.Unknown("O edital analisado não possui disciplinas ou conteúdo programático identificáveis.")
        }

        val firstCargo = results.map { it.cargo }.firstOrNull { it.isNotBlank() } ?: ""
        val firstBanca = results.map { it.banca }.firstOrNull { it.isNotBlank() } ?: ""

        val mergedDisciplinas = mutableListOf<EditalSubjectInfo>()
        val seenDisciplinas = mutableMapOf<String, EditalSubjectInfo>()

        for (res in results) {
            for (disc in res.disciplinas) {
                val key = disc.nome.trim().lowercase()
                if (key.isBlank()) continue
                val existing = seenDisciplinas[key]
                if (existing == null) {
                    seenDisciplinas[key] = disc
                    mergedDisciplinas.add(disc)
                } else {
                    val combinedSubjects = (existing.assuntos + disc.assuntos)
                        .distinctBy { it.trim().lowercase() }
                    val combinedContent = when {
                        existing.conteudoProgramatico.isBlank() -> disc.conteudoProgramatico
                        disc.conteudoProgramatico.isBlank() -> existing.conteudoProgramatico
                        existing.conteudoProgramatico == disc.conteudoProgramatico -> existing.conteudoProgramatico
                        else -> "${existing.conteudoProgramatico}\n${disc.conteudoProgramatico}"
                    }
                    val updated = existing.copy(
                        conteudoProgramatico = combinedContent,
                        assuntos = combinedSubjects
                    )
                    seenDisciplinas[key] = updated
                    val idx = mergedDisciplinas.indexOfFirst { it.nome.trim().lowercase() == key }
                    if (idx != -1) {
                        mergedDisciplinas[idx] = updated
                    }
                }
            }
        }

        val finalResult = EditalProgramResult(
            cargo = firstCargo,
            banca = firstBanca,
            disciplinas = mergedDisciplinas
        )
        return Pair(finalResult, isTruncated)
    }

    private suspend fun analyzeSingleEditalChunk(chunk: String): EditalProgramResult {
        val cacheKey = "edital_chunk_${chunk.hashCode()}"
        return AIHelper.withRetryAndTelemetry("analyzeSingleEditalChunk", cacheKey = cacheKey) {
            val systemPrompt = """
                Você é um especialista em análise de editais de concursos e vestibulares.
                Analise o trecho do edital e extraia as disciplinas, o conteúdo programático de cada uma e a lista detalhada de assuntos/tópicos.
                
                Retorne ESTRITAMENTE um objeto JSON válido (sem marcadores markdown ```json) no seguinte formato:
                {
                  "cargo": "Nome do Cargo ou Prova",
                  "banca": "Nome da Banca Organizadora",
                  "disciplinas": [
                    {
                      "nome": "Nome da Disciplina",
                      "conteudoProgramatico": "Síntese do conteúdo programático cobrado",
                      "assuntos": [
                        "Assunto 1",
                        "Assunto 2",
                        "Assunto 3"
                      ]
                    }
                  ]
                }
            """.trimIndent()

            val responseText = try {
                geminiClient.generateText(
                    prompt = "Conteúdo do Edital:\n$chunk",
                    systemInstruction = systemPrompt,
                    temperature = 0.2f
                )
            } catch (e: Exception) {
                throw e.toAIError()
            }

            if (responseText.isBlank()) {
                return@withRetryAndTelemetry EditalProgramResult()
            }

            var cleanJson = responseText.replace("```json", "").replace("```", "").trim()
            val startIdx = cleanJson.indexOf("{")
            val endIdx = cleanJson.lastIndexOf("}")
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                cleanJson = cleanJson.substring(startIdx, endIdx + 1)
            }

            try {
                json.decodeFromString<EditalProgramResult>(cleanJson)
            } catch (e: Exception) {
                EditalProgramResult()
            }
        }
    }

    suspend fun chatWithDocument(documentText: String, question: String): String {
        return AIHelper.withRetryAndTelemetry("chatWithDocument") {
            val systemPrompt = "Você é um assistente prestativo. Use o documento fornecido a seguir como base para responder à pergunta do usuário.\n\nDocumento:\n${documentText.take(25000)}"

            val response = try {
                geminiClient.generateText(
                    prompt = question,
                    systemInstruction = systemPrompt
                )
            } catch (e: Exception) {
                throw e.toAIError()
            }

            if (response.isBlank()) {
                throw AIError.Unknown("Não foi possível responder à pergunta sobre o documento.")
            }
            response
        }
    }
}
