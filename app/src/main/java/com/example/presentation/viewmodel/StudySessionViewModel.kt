package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StudySession
import com.example.data.local.Subject
import com.example.domain.repository.StudyFlowRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SessionState(
    val currentSession: StudySession? = null,
    val timeRemainingSeconds: Int = 0,
    val isRunning: Boolean = false,
    val currentPhase: String = "focus", // focus, short_break, long_break
    val cyclesCompleted: Int = 0,
    val totalTimeStudiedSeconds: Int = 0,
    val subjects: List<Subject> = emptyList()
)

class StudySessionViewModel(private val repository: StudyFlowRepository, private val gamificationManager: com.example.domain.manager.GamificationManager) : ViewModel() {
    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = combine(
        _state,
        repository.getSubjects()
    ) { state, subjects ->
        state.copy(subjects = subjects)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionState())

    private var timerJob: Job? = null

    fun startPomodoro(subjectId: Int, taskId: Int?, focusMinutes: Int = 25) {
        if (_state.value.isRunning) return
        
        _state.update { 
            it.copy(
                isRunning = true, 
                timeRemainingSeconds = if (it.timeRemainingSeconds > 0) it.timeRemainingSeconds else focusMinutes * 60,
                currentSession = StudySession(
                    subjectId = subjectId,
                    taskId = taskId,
                    techniqueUsed = "pomodoro",
                    date = System.currentTimeMillis(),
                    startTime = System.currentTimeMillis(),
                    endTime = 0L,
                    durationMinutes = 0,
                    status = "in_progress"
                )
            ) 
        }
        
        timerJob = viewModelScope.launch {
            while (_state.value.timeRemainingSeconds > 0 && _state.value.isRunning) {
                delay(1000)
                _state.update { 
                    it.copy(
                        timeRemainingSeconds = it.timeRemainingSeconds - 1,
                        totalTimeStudiedSeconds = if (it.currentPhase == "focus") it.totalTimeStudiedSeconds + 1 else it.totalTimeStudiedSeconds
                    )
                }
            }
            if (_state.value.timeRemainingSeconds <= 0) {
                handlePhaseComplete()
            }
        }
    }

    private fun handlePhaseComplete() {
        // Logic for transitioning between focus, break, long break
        val currentState = _state.value
        when (currentState.currentPhase) {
            "focus" -> {
                val newCycles = currentState.cyclesCompleted + 1
                if (newCycles % 4 == 0) {
                    // Long break
                    _state.update { it.copy(currentPhase = "long_break", timeRemainingSeconds = 15 * 60, isRunning = false, cyclesCompleted = newCycles) }
                } else {
                    // Short break
                    _state.update { it.copy(currentPhase = "short_break", timeRemainingSeconds = 5 * 60, isRunning = false, cyclesCompleted = newCycles) }
                }
            }
            "short_break", "long_break" -> {
                _state.update { it.copy(currentPhase = "focus", timeRemainingSeconds = 25 * 60, isRunning = false) }
            }
        }
    }

    fun pauseTimer() {
        _state.update { it.copy(isRunning = false) }
        timerJob?.cancel()
    }

    fun stopSession(perceivedPerformance: Int = 3, notes: String = "") {
        pauseTimer()
        val s = _state.value.currentSession ?: return
        
        viewModelScope.launch {
            val completedSession = s.copy(
                endTime = System.currentTimeMillis(),
                durationMinutes = _state.value.totalTimeStudiedSeconds / 60,
                status = "completed",
                notes = notes,
                perceivedPerformance = perceivedPerformance
            )
            repository.insertStudySession(completedSession)
            _state.update { SessionState() } // Reset
        }
    }
}
