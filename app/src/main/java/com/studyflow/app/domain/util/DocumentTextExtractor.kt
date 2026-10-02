package com.studyflow.app.domain.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.zip.ZipInputStream

object DocumentTextExtractor {
    
    private const val MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024L // 15 MB
    private const val MAX_EXTRACTED_CHARS = 50_000

    fun init(context: Context) {
        try {
            PDFBoxResourceLoader.init(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun extractText(context: Context, uri: Uri): Pair<String, String> = withContext(Dispatchers.IO) {
        var fileName = "documento"
        var fileSize = -1L

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex) ?: "documento"
                }
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    fileSize = cursor.getLong(sizeIndex)
                }
            }
        }

        if (fileSize > MAX_FILE_SIZE_BYTES) {
            throw IllegalArgumentException("O arquivo selecionado excede o limite máximo permitido de 15MB.")
        }

        val text = when {
            fileName.endsWith(".pdf", ignoreCase = true) -> extractPdf(context, uri)
            fileName.endsWith(".docx", ignoreCase = true) -> extractDocx(context, uri)
            fileName.endsWith(".txt", ignoreCase = true) || fileName.endsWith(".md", ignoreCase = true) -> extractTxt(context, uri)
            else -> extractTxt(context, uri) // Fallback
        }

        if (text.isBlank() || text.trim().length < 30) {
            throw IllegalArgumentException("O documento está vazio ou é um arquivo escaneado sem texto selecionável. Por favor, forneça um PDF com camada de texto digitalizada.")
        }

        Pair(fileName, text.take(MAX_EXTRACTED_CHARS))
    }

    private fun extractPdf(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                PDDocument.load(inputStream).use { document ->
                    if (document.numberOfPages == 0) {
                        throw IllegalArgumentException("O PDF não contém páginas.")
                    }
                    val stripper = PDFTextStripper()
                    // Limitar a extração às primeiras 40 páginas para não sobrecarregar
                    stripper.startPage = 1
                    stripper.endPage = document.numberOfPages.coerceAtMost(40)
                    val extracted = stripper.getText(document) ?: ""
                    if (extracted.trim().length < 30) {
                        throw IllegalArgumentException("PDF escaneado ou vazio: nenhuma camada de texto pesquisável foi encontrada no documento.")
                    }
                    extracted
                }
            } ?: throw IllegalArgumentException("Não foi possível abrir o fluxo de leitura do PDF.")
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("Falha ao ler o PDF: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }

    private fun extractDocx(context: Context, uri: Uri): String {
        return try {
            val stringBuilder = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val zipInputStream = ZipInputStream(inputStream)
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val buffer = ByteArray(2048)
                        var length: Int
                        while (zipInputStream.read(buffer).also { length = it } > 0) {
                            stringBuilder.append(String(buffer, 0, length))
                        }
                    }
                    zipInputStream.closeEntry()
                    entry = zipInputStream.nextEntry
                }
            }
            val xmlText = stringBuilder.toString()
            xmlText.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim()
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("Erro ao extrair texto do DOCX: ${e.message}")
        }
    }

    private fun extractTxt(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("Erro ao ler arquivo de texto: ${e.message}")
        }
    }
}
