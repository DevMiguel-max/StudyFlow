package com.studyflow.app.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.local.AnalyzedDocument
import com.studyflow.app.domain.ai.DocumentAnalyzerService
import com.studyflow.app.domain.repository.DocumentAnalyzerRepository
import com.studyflow.app.domain.util.DocumentTextExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DocumentAnalyzerState(
    val isProcessing: Boolean = false,
    val progressMessage: String = "",
    val currentDocument: AnalyzedDocument? = null,
    val error: String? = null,
    val isTruncated: Boolean = false,
    val truncationWarning: String? = null
)

class DocumentAnalyzerViewModel(
    private val repository: DocumentAnalyzerRepository,
    private val service: DocumentAnalyzerService
) : ViewModel() {

    val documents: StateFlow<List<AnalyzedDocument>> = repository.getAllDocuments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _state = MutableStateFlow(DocumentAnalyzerState())
    val state: StateFlow<DocumentAnalyzerState> = _state

    fun importAndAnalyzeDocument(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true, progressMessage = "Extraindo texto do documento...", error = null)
            try {
                // 1. Extração com ExtractedDocument
                val extracted = DocumentTextExtractor.extractText(context, uri)
                val fileName = extracted.fileName
                val textContent = extracted.text
                
                if (textContent.isBlank()) {
                    _state.value = _state.value.copy(isProcessing = false, error = "Arquivo vazio ou não foi possível extrair o texto.")
                    return@launch
                }

                // 2. Identificação de tipo
                _state.value = _state.value.copy(progressMessage = "Identificando tipo de documento...")
                val docType = service.analyzeDocument(textContent, "IDENTIFY_TYPE").trim().uppercase()
                
                val type = if (docType.contains("EDITAL")) "EDITAL" else if (docType.contains("LIVRO")) "LIVRO" else "OUTROS"

                // 3. Análise principal
                _state.value = _state.value.copy(progressMessage = "Realizando análise estruturada por IA...")
                var isTruncated = extracted.truncated
                var failedChunksCount = 0
                var totalChunksCount = 0
                val rawAnalysis = if (type == "EDITAL") {
                    val analysisResult = service.analyzeEditalProgram(textContent)
                    val editalData = analysisResult.result
                    if (analysisResult.isTruncated) isTruncated = true
                    failedChunksCount = analysisResult.failedChunks
                    totalChunksCount = analysisResult.totalChunks

                    buildString {
                        append("# ${editalData.cargo.ifBlank { "Edital" }}\n\n")
                        if (editalData.banca.isNotBlank()) append("**Banca:** ${editalData.banca}\n\n")
                        append("## Disciplinas e Conteúdo Programático\n\n")
                        editalData.disciplinas.forEach { d ->
                            append("### 📚 ${d.nome}\n")
                            if (d.conteudoProgramatico.isNotBlank()) {
                                append("${d.conteudoProgramatico}\n\n")
                            }
                            if (d.assuntos.isNotEmpty()) {
                                append("**Assuntos detalhados:**\n")
                                d.assuntos.forEach { a ->
                                    append("- $a\n")
                                }
                                append("\n")
                            }
                        }
                    }
                } else {
                    service.analyzeDocument(textContent, "ANALYSIS")
                }

                val warningMessage = when {
                    failedChunksCount > 0 -> "$failedChunksCount de $totalChunksCount blocos não puderam ser analisados — o resultado está incompleto"
                    isTruncated -> {
                        if (extracted.totalPages != null && extracted.pagesRead != null) {
                            "Foram analisadas ${extracted.pagesRead} de ${extracted.totalPages} páginas — o resultado pode estar incompleto"
                        } else {
                            "Foram analisados ${extracted.charsKept} de ${extracted.totalChars} caracteres — o resultado pode estar incompleto"
                        }
                    }
                    else -> null
                }

                val analysisResult = if (warningMessage != null) {
                    "> ⚠️ **Aviso:** $warningMessage\n\n$rawAnalysis"
                } else {
                    rawAnalysis
                }

                // 4. Salvar documento original com aviso
                val document = AnalyzedDocument(
                    title = fileName,
                    originalUri = uri.toString(),
                    textContent = textContent,
                    documentType = type,
                    analysisResult = analysisResult
                )
                
                val id = repository.insertDocument(document)
                val savedDocument = repository.getDocumentById(id.toInt())
                
                _state.value = _state.value.copy(
                    isProcessing = false, 
                    currentDocument = savedDocument,
                    isTruncated = isTruncated || failedChunksCount > 0,
                    truncationWarning = warningMessage
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(error = e.localizedMessage ?: e.message ?: "Falha no processamento do documento.")
            } finally {
                _state.value = _state.value.copy(isProcessing = false)
            }
        }
    }

    fun generateSummary(documentId: Int, type: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true, progressMessage = "Gerando resumo...", error = null)
            try {
                val doc = repository.getDocumentById(documentId) ?: return@launch
                
                val actionType = when (type) {
                    "SHORT" -> "SUMMARY_SHORT"
                    "MEDIUM" -> "SUMMARY_MEDIUM"
                    "COMPLETE" -> "SUMMARY_COMPLETE"
                    else -> "SUMMARY_SHORT"
                }

                val summary = service.analyzeDocument(doc.textContent, actionType)
                
                val updatedDoc = when (type) {
                    "SHORT" -> doc.copy(summaryShort = summary)
                    "MEDIUM" -> doc.copy(summaryMedium = summary)
                    "COMPLETE" -> doc.copy(summaryComplete = summary)
                    else -> doc
                }
                
                repository.updateDocument(updatedDoc)
                _state.value = _state.value.copy(isProcessing = false, currentDocument = updatedDoc)
                
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(error = e.message ?: "Falha ao gerar resumo.")
            } finally {
                _state.value = _state.value.copy(isProcessing = false)
            }
        }
    }

    fun deleteDocument(document: AnalyzedDocument) {
        viewModelScope.launch {
            repository.deleteDocument(document)
            if (_state.value.currentDocument?.id == document.id) {
                _state.value = _state.value.copy(currentDocument = null)
            }
        }
    }

    fun selectDocument(document: AnalyzedDocument?) {
        _state.value = _state.value.copy(currentDocument = document)
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
