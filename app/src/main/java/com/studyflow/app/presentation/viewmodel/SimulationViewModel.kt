package com.studyflow.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.local.Simulation
import com.studyflow.app.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SimulationState(
    val simulations: List<Simulation> = emptyList(),
    val isLoading: Boolean = false
)

class SimulationViewModel(private val repository: StudyFlowRepository, private val gamificationManager: com.studyflow.app.domain.manager.GamificationManager) : ViewModel() {

    private val _state = MutableStateFlow(SimulationState())
    val state: StateFlow<SimulationState> = combine(
        _state,
        repository.getAllSimulations()
    ) { state, simulations ->
        state.copy(simulations = simulations)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SimulationState())

    fun addSimulation(name: String, examType: String, totalQuestions: Int, correctAnswers: Int) {
        val wrongAnswers = totalQuestions - correctAnswers
        val score = (correctAnswers.toFloat() / totalQuestions) * 1000f // Simulate ENEM scoring out of 1000

        val simulation = Simulation(
            name = name,
            examType = examType,
            date = System.currentTimeMillis(),
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            wrongAnswers = wrongAnswers,
            score = score
        )

        viewModelScope.launch {
            repository.insertSimulation(simulation)
            gamificationManager.completeAction("SIMULATION_COMPLETED")
        }
    }
}
