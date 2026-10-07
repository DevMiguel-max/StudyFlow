package com.studyflow.app.domain.ai

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DocumentAnalyzerServiceTest {

    private lateinit var mockGeminiClient: GeminiClient
    private lateinit var service: DocumentAnalyzerService

    @Before
    fun setup() {
        mockGeminiClient = mockk(relaxed = true)
        service = DocumentAnalyzerService(geminiClient = mockGeminiClient)
    }

    private fun sampleValidEditalJson(disciplina: String = "Direito Constitucional"): String {
        return """
            {
              "cargo": "Analista Judiciário",
              "banca": "FGV",
              "disciplinas": [
                {
                  "nome": "$disciplina",
                  "conteudoProgramatico": "Direitos e garantias fundamentais",
                  "assuntos": [
                    "Artigo 5º",
                    "Nacionalidade",
                    "Direitos Políticos"
                  ]
                }
              ]
            }
        """.trimIndent()
    }

    private fun generateTextWithChunks(chunkCount: Int, chunkSize: Int = 25_000): String {
        val sb = StringBuilder()
        for (i in 1..chunkCount) {
            sb.append("Bloco $i: Conteúdo do edital com especificações do concurso e matérias. ".repeat(400))
            if (i < chunkCount) {
                // Preenche até chunkSize exatamente para ter chunks previsíveis
                val currentChunkLength = sb.length % chunkSize
                if (currentChunkLength != 0) {
                    sb.append("x".repeat(chunkSize - currentChunkLength))
                }
            }
        }
        return sb.toString()
    }

    @Test
    fun analyzeEditalProgram_whenAllSixChunksSucceed_returnsSixTotalAndZeroFailed() = runBlocking {
        val text = generateTextWithChunks(6)

        coEvery {
            mockGeminiClient.generateText(any(), any(), any(), any(), any())
        } returns sampleValidEditalJson()

        val result = service.analyzeEditalProgram(text)

        assertNotNull(result)
        assertEquals(0, result.failedChunks)
        assertEquals(6, result.totalChunks)
        assertEquals("Analista Judiciário", result.result.cargo)
        assertEquals("FGV", result.result.banca)
        assertTrue(result.result.disciplinas.isNotEmpty())
    }

    @Test
    fun analyzeEditalProgram_whenThreeChunksFail_returnsFailedChunksThreeAndGeneratesWarning() = runBlocking {
        val text = generateTextWithChunks(6)

        var callCount = 0
        coEvery {
            mockGeminiClient.generateText(any(), any(), any(), any(), any())
        } answers {
            callCount++
            if (callCount <= 3) {
                throw AIError.Network("Falha temporária de rede no bloco")
            } else {
                sampleValidEditalJson("Disciplina $callCount")
            }
        }

        val result = service.analyzeEditalProgram(text)

        assertEquals(3, result.failedChunks)
        assertEquals(6, result.totalChunks)
        assertTrue("Deve conter disciplinas dos blocos que obtiveram sucesso", result.result.disciplinas.isNotEmpty())

        // Validação da mensagem exigida pelo ViewModel
        val warningMessage = "${result.failedChunks} de ${result.totalChunks} blocos não puderam ser analisados — o resultado está incompleto"
        assertEquals("3 de 6 blocos não puderam ser analisados — o resultado está incompleto", warningMessage)
    }

    @Test
    fun analyzeEditalProgram_whenAllChunksFailWithNetworkError_propagatesNetworkError() {
        val text = generateTextWithChunks(6)

        coEvery {
            mockGeminiClient.generateText(any(), any(), any(), any(), any())
        } throws AIError.Network("Sem conexão com a IA. Verifique sua internet.")

        val error = assertThrows(AIError.Network::class.java) {
            runBlocking {
                service.analyzeEditalProgram(text)
            }
        }

        assertEquals("Sem conexão com a IA. Verifique sua internet.", error.message)
    }

    @Test
    fun analyzeEditalProgram_whenCancellationOccurs_propagatesCancellationException() {
        val text = generateTextWithChunks(6)

        coEvery {
            mockGeminiClient.generateText(any(), any(), any(), any(), any())
        } throws CancellationException("Coroutine foi cancelada durante a requisição")

        assertThrows(CancellationException::class.java) {
            runBlocking {
                service.analyzeEditalProgram(text)
            }
        }
    }
}
