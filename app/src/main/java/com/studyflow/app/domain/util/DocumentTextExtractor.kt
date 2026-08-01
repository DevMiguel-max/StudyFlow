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

object DocumentTextExtractor {
    
    fun init(context: Context) {
        PDFBoxResourceLoader.init(context)
    }

    suspend fun extractText(context: Context, uri: Uri): Pair<String, String> = withContext(Dispatchers.IO) {
        var fileName = "documento"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }

        val text = when {
            fileName.endsWith(".pdf", ignoreCase = true) -> extractPdf(context, uri)
            fileName.endsWith(".docx", ignoreCase = true) -> extractDocx(context, uri)
            fileName.endsWith(".txt", ignoreCase = true) || fileName.endsWith(".md", ignoreCase = true) -> extractTxt(context, uri)
            else -> extractTxt(context, uri) // Fallback
        }

        Pair(fileName, text)
    }

    private fun extractPdf(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                PDDocument.load(inputStream).use { document ->
                    val stripper = PDFTextStripper()
                    stripper.getText(document)
                }
            } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            "Erro ao extrair texto do PDF: ${e.message}"
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
                        val buffer = ByteArray(1024)
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
            "Erro ao extrair texto do DOCX: ${e.message}"
        }
    }

    private fun extractTxt(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            "Erro ao ler arquivo de texto: ${e.message}"
        }
    }
}
