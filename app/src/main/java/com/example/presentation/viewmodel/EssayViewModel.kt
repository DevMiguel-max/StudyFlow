package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EssaySubmission
import com.example.data.local.EssayTheme
import com.example.data.local.EssayCorrection
import com.example.data.local.MotivationalText
import com.example.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EssayState(
    val themes: List<EssayTheme> = emptyList(),
    val submissions: List<EssaySubmission> = emptyList(),
    val currentTheme: EssayTheme? = null,
    val currentSubmission: EssaySubmission? = null,
    val currentCorrection: EssayCorrection? = null,
    val currentMotivationalTexts: List<MotivationalText> = emptyList(),
    val isLoading: Boolean = false
)

class EssayViewModel(private val repository: StudyFlowRepository) : ViewModel() {
    private val _state = MutableStateFlow(EssayState())
    val state: StateFlow<EssayState> = combine(
        _state,
        repository.getAllEssayThemes(),
        repository.getAllEssaySubmissions()
    ) { state, themes, submissions ->
        state.copy(themes = themes, submissions = submissions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EssayState())

    init {
        // Pre-populate some themes if empty
        viewModelScope.launch {
            val themes = repository.getAllEssayThemes().first()
            if (themes.isEmpty()) {
                val sampleThemes = listOf(
                    EssayTheme(title = "Os desafios da educação inclusiva no Brasil", category = "Educação", difficulty = "Médio", examType = "ENEM"),
                    EssayTheme(title = "Impactos da inteligência artificial no mercado de trabalho", category = "Tecnologia", difficulty = "Difícil", examType = "ENEM"),
                    EssayTheme(title = "A preservação da Amazônia e o desenvolvimento sustentável", category = "Meio ambiente", difficulty = "Médio", examType = "ENEM")
                )
                repository.insertEssayThemes(sampleThemes)
                val newThemes = repository.getAllEssayThemes().first()
                if (newThemes.size >= 3) {
                    val texts = listOf(
                        MotivationalText(themeId = newThemes[0].id, title = "Texto I", content = "A educação inclusiva é um direito de todos.", source = "Constituição Federal", displayOrder = 1),
                        MotivationalText(themeId = newThemes[1].id, title = "Texto I", content = "A IA substituirá trabalhos, mas criará outros.", source = "Revista Tecnologia", displayOrder = 1)
                    )
                    repository.insertMotivationalTexts(texts)
                }
            }
        }
    }

    fun selectTheme(themeId: Int) {
        viewModelScope.launch {
            val theme = state.value.themes.find { it.id == themeId }
            val texts = repository.getMotivationalTextsForTheme(themeId)
            _state.update { it.copy(currentTheme = theme, currentMotivationalTexts = texts, currentSubmission = null, currentCorrection = null) }
        }
    }

    fun loadSubmission(submissionId: Int) {
        viewModelScope.launch {
            val submission = repository.getEssaySubmissionById(submissionId)
            val correction = repository.getEssayCorrectionForSubmission(submissionId)
            val theme = state.value.themes.find { it.id == submission?.themeId }
            val texts = if (submission?.themeId != null) repository.getMotivationalTextsForTheme(submission.themeId) else emptyList()
            _state.update { it.copy(currentSubmission = submission, currentCorrection = correction, currentTheme = theme, currentMotivationalTexts = texts) }
        }
    }

    fun saveSubmission(text: String, timeSpentSeconds: Int, status: String) {
        val themeId = state.value.currentTheme?.id ?: return
        val currentSub = state.value.currentSubmission
        
        viewModelScope.launch {
            val wordCount = text.split("\\s+".toRegex()).count { it.isNotBlank() }
            if (currentSub != null) {
                val updated = currentSub.copy(text = text, timeSpentSeconds = timeSpentSeconds, status = status, wordCount = wordCount, charCount = text.length)
                repository.updateEssaySubmission(updated)
                _state.update { it.copy(currentSubmission = updated) }
            } else {
                val newSub = EssaySubmission(
                    themeId = themeId,
                    text = text,
                    date = System.currentTimeMillis(),
                    timeSpentSeconds = timeSpentSeconds,
                    status = status,
                    wordCount = wordCount,
                    charCount = text.length
                )
                val id = repository.insertEssaySubmission(newSub)
                _state.update { it.copy(currentSubmission = newSub.copy(id = id.toInt())) }
            }
        }
    }

    fun requestCorrection(submissionId: Int) {
        viewModelScope.launch {
            // Mocking a correction
            val correction = EssayCorrection(
                submissionId = submissionId,
                comp1 = 160, comp2 = 200, comp3 = 160, comp4 = 160, comp5 = 120,
                totalScore = 800,
                strengths = "Boa compreensão do tema e uso adequado do repertório.",
                weaknesses = "Alguns desvios gramaticais e proposta de intervenção incompleta.",
                suggestions = "Revise o uso de vírgulas e detalhe mais a ação da proposta de intervenção.",
                date = System.currentTimeMillis()
            )
            repository.insertEssayCorrection(correction)
            
            val submission = repository.getEssaySubmissionById(submissionId)
            if (submission != null) {
                repository.updateEssaySubmission(submission.copy(status = "graded"))
            }
            loadSubmission(submissionId)
        }
    }
}
