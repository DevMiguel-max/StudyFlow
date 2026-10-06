package com.studyflow.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentTextExtractorTest {

    @Test
    fun unsupportedExtensionAndMime_throwsUnsupportedFormatError() {
        // Testa extensão não suportada
        val ex1 = assertThrows(IllegalArgumentException::class.java) {
            DocumentTextExtractor.validateFormat("planilha.xlsx", null)
        }
        assertEquals("Formato não suportado. Use PDF, DOCX, TXT ou MD.", ex1.message)

        // Testa MIME não suportado
        val ex2 = assertThrows(IllegalArgumentException::class.java) {
            DocumentTextExtractor.validateFormat("arquivo", "application/zip")
        }
        assertEquals("Formato não suportado. Use PDF, DOCX, TXT ou MD.", ex2.message)
    }

    @Test
    fun supportedFormats_passValidation() {
        // Não deve lançar exceção para extensões e MIMEs válidos
        DocumentTextExtractor.validateFormat("edital.pdf", "application/pdf")
        DocumentTextExtractor.validateFormat("documento.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        DocumentTextExtractor.validateFormat("notas.txt", "text/plain")
        DocumentTextExtractor.validateFormat("readme.md", "text/markdown")
    }

    @Test
    fun binaryContent_withExcessiveControlChars_throwsBinaryError() {
        // Simula arquivo com muitos caracteres de controle ou NUL
        val binaryText = "Inicio\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008Fim"
        assertTrue(DocumentTextExtractor.isBinaryContent(binaryText))

        val ex = assertThrows(IllegalArgumentException::class.java) {
            DocumentTextExtractor.validateContent(binaryText)
        }
        assertTrue(ex.message?.contains("Arquivo inválido ou binário") == true)
    }

    @Test
    fun emptyOrScannedFile_under30Chars_throwsScannedMessage() {
        val blankText = "   "
        val shortText = "Texto curto < 30"

        val ex1 = assertThrows(IllegalArgumentException::class.java) {
            DocumentTextExtractor.validateContent(blankText)
        }
        assertEquals(
            "PDF escaneado ou vazio: nenhuma camada de texto pesquisável foi encontrada no documento.",
            ex1.message
        )

        val ex2 = assertThrows(IllegalArgumentException::class.java) {
            DocumentTextExtractor.validateContent(shortText)
        }
        assertEquals(
            "PDF escaneado ou vazio: nenhuma camada de texto pesquisável foi encontrada no documento.",
            ex2.message
        )
    }

    @Test
    fun validText_withinLimits_returnsExtractedDocumentWithoutTruncation() {
        val sampleText = "Disciplina de Português e Matemática para Concurso Público com carga horária suficiente e conteúdo programático completo."
        val result = DocumentTextExtractor.processExtractedText(
            rawText = sampleText,
            totalPages = 20,
            pagesRead = 20,
            fileName = "edital.pdf"
        )

        assertEquals("edital.pdf", result.fileName)
        assertEquals(sampleText, result.text)
        assertFalse(result.truncated)
        assertEquals(20, result.totalPages)
        assertEquals(20, result.pagesRead)
        assertEquals(sampleText.length, result.totalChars)
        assertEquals(sampleText.length, result.charsKept)
    }

    @Test
    fun fileExceedingCharLimit_flagsTruncatedAndCutsText() {
        val repeatCount = 160_000
        val hugeText = "Conteúdo extenso para simular edital de concurso público que ultrapassa o limite total de caracteres. ".repeat(1600)
        assertTrue(hugeText.length > DocumentTextExtractor.MAX_TOTAL_CHARS)

        val result = DocumentTextExtractor.processExtractedText(
            rawText = hugeText,
            totalPages = null,
            pagesRead = null,
            fileName = "edital_grande.txt"
        )

        assertTrue("Deve sinalizar truncated == true quando excede o limite de caracteres", result.truncated)
        assertEquals(hugeText.length, result.totalChars)
        assertEquals(DocumentTextExtractor.MAX_TOTAL_CHARS, result.charsKept)
        assertEquals(DocumentTextExtractor.MAX_TOTAL_CHARS, result.text.length)
    }

    @Test
    fun fileExceedingPageLimit_flagsTruncated() {
        val sampleText = "Texto de edital com muitas páginas analisadas parcialmente."
        val result = DocumentTextExtractor.processExtractedText(
            rawText = sampleText,
            totalPages = 80,
            pagesRead = 40,
            fileName = "edital_muitas_paginas.pdf"
        )

        assertTrue("Deve sinalizar truncated == true quando páginas lidas < total de páginas", result.truncated)
        assertEquals(80, result.totalPages)
        assertEquals(40, result.pagesRead)
    }
}
