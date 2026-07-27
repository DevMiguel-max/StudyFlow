package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GeneratedMaterial
import com.example.data.local.GeneratedQuestion
import com.example.data.local.Flashcard
import com.example.data.local.Subject
import com.example.domain.ai.MaterialGeneratorService
import com.example.domain.repository.MaterialGeneratorRepository
import com.example.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MaterialGeneratorState(
    val isGenerating: Boolean = false,
    val generationMessage: String = "",
    val error: String? = null,
    val generatedSummary: String? = null,
    val generatedQuestions: List<GeneratedQuestion> = emptyList(),
    val generatedFlashcards: List<Flashcard> = emptyList()
)

class MaterialGeneratorViewModel(
    private val repository: MaterialGeneratorRepository,
    private val service: MaterialGeneratorService,
    private val studyFlowRepository: StudyFlowRepository,
    private val docRepository: com.example.domain.repository.DocumentAnalyzerRepository
) : ViewModel() {

    val analyzedDocuments: StateFlow<List<com.example.data.local.AnalyzedDocument>> = docRepository.getAllDocuments().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val subjects: StateFlow<List<Subject>> = studyFlowRepository.getSubjects()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedMaterials: StateFlow<List<GeneratedMaterial>> = repository.getAllMaterials()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    val savedQuestions: StateFlow<List<GeneratedQuestion>> = repository.getAllQuestions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _state = MutableStateFlow(MaterialGeneratorState())
    val state: StateFlow<MaterialGeneratorState> = _state

    fun generateSummary(sourceText: String, type: String, title: String) {
        if (sourceText.isBlank()) {
            _state.value = _state.value.copy(error = "O texto fonte não pode estar vazio.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true, generationMessage = "Gerando resumo ($type)...", error = null, generatedSummary = null)
            try {
                val result = service.generateMaterial(sourceText, type)
                if (result.startsWith("Erro")) {
                    _state.value = _state.value.copy(error = result)
                } else {
                    // Save automatically
                    val material = GeneratedMaterial(
                        type = type,
                        title = title,
                        content = result,
                        source = "Geração Inteligente"
                    )
                    repository.insertMaterial(material)
                    _state.value = _state.value.copy(generatedSummary = result)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Erro ao gerar resumo.")
            } finally {
                _state.value = _state.value.copy(isGenerating = false)
            }
        }
    }

    fun generateFlashcards(sourceText: String, count: Int, difficulty: String, subjectId: Int) {
        if (sourceText.isBlank()) {
            _state.value = _state.value.copy(error = "O texto fonte não pode estar vazio.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true, generationMessage = "Gerando $count flashcards ($difficulty)...", error = null, generatedFlashcards = emptyList())
            val subjectName = subjects.value.find { it.id == subjectId }?.name ?: "Geral"
            val actualDifficulty = if (difficulty == "ADAPTATIVO") getAdaptiveDifficulty(subjectName) else difficulty
            
            try {
                val flashcards = service.generateFlashcards(sourceText, count, actualDifficulty, subjectId, subjectName)
                
                if (flashcards.isEmpty()) {
                    _state.value = _state.value.copy(error = "Não foi possível gerar os flashcards.")
                } else {
                    repository.insertFlashcards(flashcards)
                    _state.value = _state.value.copy(generatedFlashcards = flashcards)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Erro ao gerar flashcards.")
            } finally {
                _state.value = _state.value.copy(isGenerating = false)
            }
        }
    }
    
    fun generateQuestions(sourceText: String, count: Int, difficulty: String, type: String, subjectId: Int) {
        if (sourceText.isBlank()) {
            _state.value = _state.value.copy(error = "O texto fonte não pode estar vazio.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true, generationMessage = "Gerando $count questões ($type)...", error = null, generatedQuestions = emptyList())
            val subjectName = subjects.value.find { it.id == subjectId }?.name ?: "Geral"
            val actualDifficulty = if (difficulty == "ADAPTATIVO") getAdaptiveDifficulty(subjectName) else difficulty
            
            try {
                val questions = service.generateQuestions(sourceText, count, actualDifficulty, subjectId, subjectName)
                
                if (questions.isEmpty()) {
                    _state.value = _state.value.copy(error = "Não foi possível gerar as questões.")
                } else {
                    repository.insertQuestions(questions)
                    _state.value = _state.value.copy(generatedQuestions = questions)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Erro ao gerar questões.")
            } finally {
                _state.value = _state.value.copy(isGenerating = false)
            }
        }
    }
    
    fun deleteMaterial(material: GeneratedMaterial) {
        viewModelScope.launch {
            repository.deleteMaterial(material)
        }
    }

    fun getAdaptiveDifficulty(subjectName: String): String {
        val subjectQuestions = savedQuestions.value.filter { it.subject == subjectName && it.isAnswered }
        if (subjectQuestions.size < 3) return "MÉDIO"
        val recent = subjectQuestions.sortedByDescending { it.createdAt }.take(5)
        val correctCount = recent.count { it.wasCorrect }
        val rate = correctCount.toFloat() / recent.size
        return when {
            rate >= 0.8f -> "DIFÍCIL"
            rate <= 0.4f -> "FÁCIL"
            else -> "MÉDIO"
        }
    }

    fun clearState() {
        _state.value = MaterialGeneratorState()
    }
    
    fun answerQuestion(question: GeneratedQuestion, selectedAnswer: String) {
        viewModelScope.launch {
            val wasCorrect = question.correctAnswer.trim().equals(selectedAnswer.trim(), ignoreCase = true) || 
                             question.correctAnswer.contains(selectedAnswer, ignoreCase = true)
            val updated = question.copy(isAnswered = true, wasCorrect = wasCorrect)
            repository.updateQuestion(updated)
        }
    }
}
