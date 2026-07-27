package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.domain.ai.MentorService
import com.example.domain.repository.MentorRepository
import com.example.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MentorViewModel(
    private val mentorRepository: MentorRepository,
    private val studyFlowRepository: StudyFlowRepository,
    private val mentorService: MentorService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MentorUiState())
    val uiState: StateFlow<MentorUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                mentorRepository.getMentorProfile(),
                mentorRepository.getAllMentorRecommendations(),
                mentorRepository.getActiveSmartMissions()
            ) { profile, recommendations, missions ->
                MentorUiState(
                    profile = profile ?: MentorProfile(),
                    recommendations = recommendations,
                    activeMissions = missions,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun updateProfile(profile: MentorProfile) {
        viewModelScope.launch {
            mentorRepository.saveMentorProfile(profile)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun runAnalysis() {
        viewModelScope.launch {
            val currentProfile = _uiState.value.profile
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val subjects = studyFlowRepository.getSubjects().firstOrNull() ?: emptyList()
            val tasks = studyFlowRepository.getTasks().firstOrNull() ?: emptyList()
            val sessions = studyFlowRepository.getAllStudySessions().firstOrNull() ?: emptyList()
            val simulations = studyFlowRepository.getAllSimulations().firstOrNull() ?: emptyList()
            val goals = emptyList<StudyGoal>() // Just for now
            
            try {
                val result = mentorService.analyzeProgress(
                    profile = currentProfile,
                    subjects = subjects,
                    tasks = tasks,
                    sessions = sessions,
                    goals = goals,
                    simulations = simulations
                )
                
                // Save recommendations
                result.recommendations.forEach { dto ->
                    mentorRepository.insertMentorRecommendation(
                        MentorRecommendation(text = dto.text, reason = dto.reason, type = dto.type)
                    )
                }
                
                // Save missions
                result.missions.forEach { dto ->
                    mentorRepository.insertSmartMission(
                        SmartMission(
                            title = dto.title, 
                            description = dto.description, 
                            type = dto.type, 
                            targetAmount = dto.targetAmount, 
                            xpReward = dto.xpReward
                        )
                    )
                }
                
                mentorRepository.insertMentorHistory(
                    MentorHistory(eventType = "AnalysisRun", details = result.overallProgressAssessment)
                )
                
                _uiState.update { it.copy(lastAssessment = result.overallProgressAssessment) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Erro ao gerar análise.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun markRecommendationAsRead(recommendation: MentorRecommendation) {
        viewModelScope.launch {
            mentorRepository.updateMentorRecommendation(recommendation.copy(isRead = true))
        }
    }
    
    fun completeMission(mission: SmartMission) {
        viewModelScope.launch {
            mentorRepository.updateSmartMission(mission.copy(isCompleted = true))
        }
    }
}

data class MentorUiState(
    val profile: MentorProfile = MentorProfile(),
    val recommendations: List<MentorRecommendation> = emptyList(),
    val activeMissions: List<SmartMission> = emptyList(),
    val isLoading: Boolean = true,
    val lastAssessment: String? = null,
    val error: String? = null
)
