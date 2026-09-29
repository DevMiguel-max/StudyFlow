package com.studyflow.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.local.EssaySubmission
import com.studyflow.app.data.local.EssayTheme
import com.studyflow.app.data.local.EssayCorrection
import com.studyflow.app.data.local.MotivationalText
import com.studyflow.app.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import com.studyflow.app.domain.ai.AIEssayCorrectorService
import com.studyflow.app.domain.ai.AICorrectionResult
import com.studyflow.app.domain.ai.DetailedError

data class EssayState(
    val themes: List<EssayTheme> = emptyList(),
    val submissions: List<EssaySubmission> = emptyList(),
    val currentTheme: EssayTheme? = null,
    val currentSubmission: EssaySubmission? = null,
    val currentCorrection: EssayCorrection? = null,
    val currentMotivationalTexts: List<MotivationalText> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class EssayViewModel(
    private val repository: StudyFlowRepository, 
    private val gamificationManager: com.studyflow.app.domain.manager.GamificationManager,
    private val aiService: AIEssayCorrectorService = AIEssayCorrectorService()
) : ViewModel() {

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
                gamificationManager.completeAction("ESSAY_COMPLETED")
                _state.update { it.copy(currentSubmission = newSub.copy(id = id.toInt())) }
            }
        }
    }

    fun submitForCorrection(text: String, timeSpentSeconds: Int) {
        val themeId = state.value.currentTheme?.id ?: return
        val currentSub = state.value.currentSubmission
        
        viewModelScope.launch {
            val wordCount = text.split("\\s+".toRegex()).count { it.isNotBlank() }
            val submissionId: Int
            
            if (currentSub != null) {
                val updated = currentSub.copy(text = text, timeSpentSeconds = timeSpentSeconds, status = "sent", wordCount = wordCount, charCount = text.length)
                repository.updateEssaySubmission(updated)
                _state.update { it.copy(currentSubmission = updated) }
                submissionId = updated.id
            } else {
                val newSub = EssaySubmission(
                    themeId = themeId,
                    text = text,
                    date = System.currentTimeMillis(),
                    timeSpentSeconds = timeSpentSeconds,
                    status = "sent",
                    wordCount = wordCount,
                    charCount = text.length
                )
                submissionId = repository.insertEssaySubmission(newSub).toInt()
                gamificationManager.completeAction("ESSAY_COMPLETED")
                _state.update { it.copy(currentSubmission = newSub.copy(id = submissionId)) }
            }
            
            requestCorrection(submissionId)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun requestCorrection(submissionId: Int, model: String = com.studyflow.app.domain.ai.AIConfig.MODEL, rigor: String = "Normal", detailed: Boolean = true) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val submission = repository.getEssaySubmissionById(submissionId) ?: throw Exception("Redação não encontrada.")
                if (submission.text.trim().split("\\s+".toRegex()).size < 50) {
                    throw Exception("O texto é muito curto para ser corrigido. Escreva pelo menos 50 palavras.")
                }
                
                val theme = state.value.themes.find { it.id == submission.themeId } ?: repository.getAllEssayThemes().first().find { it.id == submission.themeId }
                val themeTitle = theme?.title ?: "Tema livre"
                
                val texts = repository.getMotivationalTextsForTheme(submission.themeId)
                val motivationalTexts = texts.joinToString("\n\n") { "${it.title}: ${it.content}" }
                
                val result = aiService.correctEssay(
                    modelName = model,
                    temperature = if (rigor == "Alto") 0.2f else 0.7f,
                    essayText = submission.text,
                    themeTitle = themeTitle,
                    motivationalTexts = motivationalTexts
                )
                
                val json = Json { ignoreUnknownKeys = true }
                
                val compDetailsMap = mapOf(
                    "comp1" to result.comp1,
                    "comp2" to result.comp2,
                    "comp3" to result.comp3,
                    "comp4" to result.comp4,
                    "comp5" to result.comp5
                )
                
                val correction = EssayCorrection(
                    submissionId = submissionId,
                    comp1 = result.comp1.score,
                    comp2 = result.comp2.score,
                    comp3 = result.comp3.score,
                    comp4 = result.comp4.score,
                    comp5 = result.comp5.score,
                    totalScore = result.totalScore,
                    strengths = "Ver detalhes nas competências.",
                    weaknesses = "Ver detalhes nas competências.",
                    suggestions = "Ver detalhes nas competências.",
                    date = System.currentTimeMillis(),
                    generalComment = result.generalComment,
                    essayLevel = result.essayLevel,
                    performanceEstimate = result.performanceEstimate,
                    revisedVersion = result.revisedVersion,
                    detailedAnalysisJson = json.encodeToString(result.detailedErrors),
                    competenciesDetailsJson = json.encodeToString(compDetailsMap),
                    modelUsed = model,
                    isDetailed = detailed
                )
                
                repository.insertEssayCorrection(correction)
                repository.updateEssaySubmission(submission.copy(status = "graded"))
                
                loadSubmission(submissionId)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Ocorreu um erro ao corrigir a redação.") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
