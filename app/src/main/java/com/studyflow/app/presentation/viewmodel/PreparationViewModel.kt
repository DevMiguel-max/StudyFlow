package com.studyflow.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.local.StudyGoal
import com.studyflow.app.data.local.EssaySubmission
import com.studyflow.app.data.local.Simulation
import com.studyflow.app.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PreparationState(
    val currentGoal: StudyGoal? = null,
    val allGoals: List<StudyGoal> = emptyList(),
    val upcomingExamDays: Int? = null,
    val totalHoursStudied: Int = 0,
    val essaysCompleted: Int = 0,
    val simulationsCompleted: Int = 0
)

class PreparationViewModel(private val repository: StudyFlowRepository) : ViewModel() {

    val state: StateFlow<PreparationState> = combine(
        repository.getAllStudyGoals(),
        repository.getAllEssaySubmissions(),
        repository.getAllSimulations(),
        repository.getAllStudySessions()
    ) { goals, essays, simulations, sessions ->
        val nextGoal = goals.filter { it.date != null && it.date > System.currentTimeMillis() }
            .minByOrNull { it.date!! }
        
        val daysToExam = nextGoal?.date?.let {
            ((it - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        }
        
        val totalMinutes = sessions.filter { it.status == "completed" }.sumOf { it.durationMinutes }
        
        PreparationState(
            currentGoal = nextGoal,
            allGoals = goals,
            upcomingExamDays = daysToExam,
            totalHoursStudied = totalMinutes / 60,
            essaysCompleted = essays.count { it.status == "completed" || it.status == "graded" },
            simulationsCompleted = simulations.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreparationState())
}
