package com.example.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AnalyzedDocument
import com.example.domain.ai.DocumentAnalyzerService
import com.example.domain.repository.DocumentAnalyzerRepository
import com.example.domain.util.DocumentTextExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DocumentAnalyzerState(
    val isProcessing: Boolean = false,
    val progressMessage: String = "",
    val currentDocument: AnalyzedDocument? = null,
    val error: String? = null
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
                // 1. Extração
                val (fileName, textContent) = DocumentTextExtractor.extractText(context, uri)
                
                if (textContent.isBlank()) {
                    _state.value = _state.value.copy(isProcessing = false, error = "Arquivo vazio ou não foi possível extrair o texto.")
                    return@launch
                }

                // 2. Identificação de tipo
                _state.value = _state.value.copy(progressMessage = "Identificando tipo de documento...")
                val docType = service.analyzeDocument(textContent, "IDENTIFY_TYPE").trim().uppercase()
                
                val type = if (docType.contains("EDITAL")) "EDITAL" else if (docType.contains("LIVRO")) "LIVRO" else "OUTROS"

                // 3. Análise principal
                _state.value = _state.value.copy(progressMessage = "Realizando análise por IA...")
                val analysisType = if (type == "EDITAL") "EDITAL_ANALYSIS" else "ANALYSIS"
                val analysisResult = service.analyzeDocument(textContent, analysisType)

                // 4. Salvar documento original
                val document = AnalyzedDocument(
                    title = fileName,
                    originalUri = uri.toString(),
                    textContent = textContent,
                    documentType = type,
                    analysisResult = analysisResult
                )
                
                val id = repository.insertDocument(document)
                val savedDocument = repository.getDocumentById(id.toInt())
                
                _state.value = _state.value.copy(isProcessing = false, currentDocument = savedDocument)

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = _state.value.copy(isProcessing = false, error = "Falha no processamento: ${e.message}")
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
                _state.value = _state.value.copy(isProcessing = false, error = "Falha ao gerar resumo.")
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
