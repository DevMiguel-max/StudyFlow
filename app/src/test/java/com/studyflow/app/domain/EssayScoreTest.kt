package com.studyflow.app.domain

import com.studyflow.app.domain.ai.AICorrectionResult
import com.studyflow.app.domain.ai.AIError
import com.studyflow.app.domain.ai.AIEssayCorrectorService
import com.studyflow.app.domain.ai.GeminiClient
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EssayScoreTest {

    private lateinit var mockGeminiClient: GeminiClient
    private lateinit var service: AIEssayCorrectorService

    @Before
    fun setup() {
        mockGeminiClient = mockk(relaxed = true)
        service = AIEssayCorrectorService(geminiClient = mockGeminiClient)
    }

    private fun sampleValidJson(): String {
        return """
            {
              "comp1": {
                "score": 160,
                "explanation": "Bom domínio da norma padrão com desvios pontuais.",
                "strengths": "Excelente concordância",
                "weaknesses": "Pequenos desvios de pontuação",
                "suggestions": "Atenção ao uso da vírgula"
              },
              "comp2": {
                "score": 160,
                "explanation": "Compreendeu a proposta e desenvolveu o tema com repertório legítimo.",
                "strengths": "Citação pertinente de filósofo",
                "weaknesses": "Poderia aprofundar a relação com o tema",
                "suggestions": "Vincular mais a citação aos argumentos"
              },
              "comp3": {
                "score": 160,
                "explanation": "Projeto de texto estratégico e argumentação consistente.",
                "strengths": "Defesa clara de ponto de vista",
                "weaknesses": "Uma das teses foi pouco aprofundada",
                "suggestions": "Equilibrar o peso dos parágrafos de desenvolvimento"
              },
              "comp4": {
                "score": 160,
                "explanation": "Articulação eficiente das partes do texto com conectivos diversificados.",
                "strengths": "Conectivos interparágrafos adequados",
                "weaknesses": "Repetição ocasional de conectivo",
                "suggestions": "Diversificar operadores argumentativos"
              },
              "comp5": {
                "score": 200,
                "explanation": "Proposta de intervenção completa com os cinco elementos.",
                "strengths": "Agente, ação, meio, efeito e detalhamento presentes",
                "weaknesses": "Nenhuma",
                "suggestions": "Manter a estrutura completa"
              },
              "totalScore": 840,
              "generalComment": "Redação consistente, bem estruturada e dentro do padrão ENEM.",
              "essayLevel": "Avançado",
              "performanceEstimate": "800 - 880",
              "detailedErrors": [
                {
                  "type": "Pontuação",
                  "snippet": "no entanto o cenário",
                  "explanation": "Falta vírgula após a conjunção adversativa",
                  "suggestion": "no entanto, o cenário"
                }
              ],
              "revisedVersion": "Texto revisado de exemplo."
            }
        """.trimIndent()
    }

    @Test
    fun correctEssay_withValidJson_parsesAllFieldsAndValidatesSum() = runBlocking {
        coEvery {
            mockGeminiClient.generateText(any(), any(), any(), any(), any())
        } returns sampleValidJson()

        val result = service.correctEssay(
            modelName = "gemini-2.5-flash",
            temperature = 0.3f,
            essayText = "Texto da redação com mais de 30 linhas...",
            themeTitle = "Invisibilidade do trabalho de cuidado no Brasil",
            motivationalTexts = "Textos motivadores..."
        )

        assertNotNull(result)
        assertEquals(160, result.comp1.score)
        assertEquals(160, result.comp2.score)
        assertEquals(160, result.comp3.score)
        assertEquals(160, result.comp4.score)
        assertEquals(200, result.comp5.score)
        assertEquals(840, result.totalScore)

        val calculatedSum = result.comp1.score + result.comp2.score + result.comp3.score + result.comp4.score + result.comp5.score
        assertEquals(calculatedSum, result.totalScore)
        assertEquals("Avançado", result.essayLevel)
        assertEquals(1, result.detailedErrors.size)
    }

    @Test
    fun correctEssay_withMalformedJson_throwsAIError() {
        coEvery {
            mockGeminiClient.generateText(any(), any(), any(), any(), any())
        } returns """{ "comp1": { "score": 160, "explanation": "incompleto" """

        val error = assertThrows(AIError::class.java) {
            runBlocking {
                service.correctEssay(
                    modelName = "gemini-2.5-flash",
                    temperature = 0.3f,
                    essayText = "Texto...",
                    themeTitle = "Tema...",
                    motivationalTexts = "Motivadores..."
                )
            }
        }

        assertTrue(error is AIError.Unknown)
        assertTrue(error.message?.contains("Falha ao interpretar") == true)
    }

    @Test
    fun correctEssay_withCompetencyAbove200_documentsCurrentBehavior() = runBlocking {
        // Simula resposta da IA onde a competência 1 veio como 250 (acima do limite do ENEM de 200)
        val jsonWithInvalidScore = sampleValidJson().replace(
            "\"comp1\": {\n    \"score\": 160",
            "\"comp1\": {\n    \"score\": 250"
        )

        coEvery {
            mockGeminiClient.generateText(any(), any(), any(), any(), any())
        } returns jsonWithInvalidScore

        val result = service.correctEssay(
            modelName = "gemini-2.5-flash",
            temperature = 0.3f,
            essayText = "Texto...",
            themeTitle = "Tema...",
            motivationalTexts = "Motivadores..."
        )

        // BUG IDENTIFICADO NA PRODUÇÃO:
        // AIEssayCorrectorService não valida ou limita (clamp) o valor a 200, retornando 250 diretamente.
        assertEquals(250, result.comp1.score)
        assertTrue("BUG: Código atual permite competência acima de 200 pontos", result.comp1.score > 200)
    }

    @Test
    fun correctEssay_withTotalScoreDifferentFromSum_documentsCurrentBehavior() = runBlocking {
        // Simula resposta da IA onde a soma das competências é 840, mas totalScore retornado é 900
        val jsonWithInconsistentTotal = sampleValidJson().replace(
            "\"totalScore\": 840",
            "\"totalScore\": 900"
        )

        coEvery {
            mockGeminiClient.generateText(any(), any(), any(), any(), any())
        } returns jsonWithInconsistentTotal

        val result = service.correctEssay(
            modelName = "gemini-2.5-flash",
            temperature = 0.3f,
            essayText = "Texto...",
            themeTitle = "Tema...",
            motivationalTexts = "Motivadores..."
        )

        val actualSum = result.comp1.score + result.comp2.score + result.comp3.score + result.comp4.score + result.comp5.score
        assertEquals(840, actualSum)
        assertEquals(900, result.totalScore)

        // BUG IDENTIFICADO NA PRODUÇÃO:
        // AIEssayCorrectorService não valida a consistência entre a soma das competências e o totalScore.
        assertNotEquals("BUG: Código atual aceita totalScore inconsistente com a soma", actualSum, result.totalScore)
    }
}
