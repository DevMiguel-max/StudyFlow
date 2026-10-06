package com.studyflow.app.domain.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.zip.ZipInputStream

data class ExtractedDocument(
    val fileName: String,
    val text: String,
    val truncated: Boolean,
    val totalPages: Int?,
    val pagesRead: Int?,
    val totalChars: Int,
    val charsKept: Int
)

object DocumentTextExtractor {
    
    private const val MAX_FILE_SIZE_BYTES = 25 * 1024 * 1024L // 25 MB
    const val MAX_CHUNKS = 6
    const val CHUNK_SIZE = 25_000
    const val MAX_TOTAL_CHARS = MAX_CHUNKS * CHUNK_SIZE // 150.000 caracteres
    const val MAX_PAGES_TO_READ = 40

    fun init(context: Context) {
        try {
            PDFBoxResourceLoader.init(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun validateFormat(fileName: String, mimeType: String?) {
        val lowerMime = mimeType?.lowercase()
        val isPdf = fileName.endsWith(".pdf", ignoreCase = true) || lowerMime == "application/pdf"
        val isDocx = fileName.endsWith(".docx", ignoreCase = true) || lowerMime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        val isTxt = fileName.endsWith(".txt", ignoreCase = true) || lowerMime == "text/plain"
        val isMd = fileName.endsWith(".md", ignoreCase = true) || lowerMime == "text/markdown" || lowerMime == "text/x-markdown"

        if (!isPdf && !isDocx && !isTxt && !isMd) {
            throw IllegalArgumentException("Formato não suportado. Use PDF, DOCX, TXT ou MD.")
        }
    }

    fun validateContent(rawText: String) {
        if (isBinaryContent(rawText)) {
            throw IllegalArgumentException("Arquivo inválido ou binário. O conteúdo de texto contém caracteres não legíveis.")
        }

        if (rawText.isBlank() || rawText.trim().length < 30) {
            throw IllegalArgumentException("PDF escaneado ou vazio: nenhuma camada de texto pesquisável foi encontrada no documento.")
        }
    }

    fun processExtractedText(
        rawText: String,
        totalPages: Int?,
        pagesRead: Int?,
        fileName: String
    ): ExtractedDocument {
        validateContent(rawText)

        val totalChars = rawText.length
        val truncatedByChars = totalChars > MAX_TOTAL_CHARS
        val truncatedByPages = totalPages != null && pagesRead != null && pagesRead < totalPages
        val isTruncated = truncatedByChars || truncatedByPages

        val finalText = if (truncatedByChars) rawText.take(MAX_TOTAL_CHARS) else rawText
        val charsKept = finalText.length

        return ExtractedDocument(
            fileName = fileName,
            text = finalText,
            truncated = isTruncated,
            totalPages = totalPages,
            pagesRead = pagesRead,
            totalChars = totalChars,
            charsKept = charsKept
        )
    }

    suspend fun extractText(context: Context, uri: Uri): ExtractedDocument = withContext(Dispatchers.IO) {
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
            throw IllegalArgumentException("O arquivo selecionado excede o limite máximo permitido de 25MB.")
        }

        val mimeType = try {
            context.contentResolver.getType(uri)?.lowercase()
        } catch (e: Exception) {
            null
        }

        validateFormat(fileName, mimeType)

        val isPdf = fileName.endsWith(".pdf", ignoreCase = true) || mimeType == "application/pdf"
        val isDocx = fileName.endsWith(".docx", ignoreCase = true) || mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        val isTxt = fileName.endsWith(".txt", ignoreCase = true) || mimeType == "text/plain"
        val isMd = fileName.endsWith(".md", ignoreCase = true) || mimeType == "text/markdown" || mimeType == "text/x-markdown"

        val (rawText, totalPages, pagesRead) = when {
            isPdf -> extractPdfWithPages(context, uri)
            isDocx -> Triple(extractDocx(context, uri), null, null)
            isTxt || isMd -> Triple(extractTxt(context, uri), null, null)
            else -> throw IllegalArgumentException("Formato não suportado. Use PDF, DOCX, TXT ou MD.")
        }

        processExtractedText(rawText, totalPages, pagesRead, fileName)
    }

    private fun extractPdfWithPages(context: Context, uri: Uri): Triple<String, Int, Int> {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Não foi possível abrir o fluxo de leitura do PDF.")

        return inputStream.use { stream ->
            PDDocument.load(stream).use { document ->
                val totalPages = document.numberOfPages
                if (totalPages == 0) {
                    throw IllegalArgumentException("O PDF não contém páginas.")
                }

                val pagesToRead = totalPages.coerceAtMost(MAX_PAGES_TO_READ)
                val stripper = PDFTextStripper()
                stripper.startPage = 1
                stripper.endPage = pagesToRead

                val extracted = stripper.getText(document) ?: ""
                Triple(extracted, totalPages, pagesToRead)
            }
        }
    }

    private fun extractDocx(context: Context, uri: Uri): String {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Não foi possível abrir o fluxo do arquivo DOCX.")

        return inputStream.use { stream ->
            val stringBuilder = StringBuilder()
            val zipInputStream = ZipInputStream(stream)
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
            val xmlText = stringBuilder.toString()
            xmlText.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim()
        }
    }

    private fun extractTxt(context: Context, uri: Uri): String {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Não foi possível abrir o fluxo do arquivo de texto.")

        return inputStream.use { stream ->
            stream.bufferedReader().readText()
        }
    }

    fun isBinaryContent(text: String): Boolean {
        if (text.contains('\u0000')) return true
        var controlChars = 0
        val sample = text.take(2000)
        for (ch in sample) {
            if (ch.isISOControl() && ch != '\n' && ch != '\r' && ch != '\t') {
                controlChars++
            }
        }
        return sample.isNotEmpty() && (controlChars.toDouble() / sample.length > 0.05)
    }
}
