package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.StudyPlan
import com.example.data.local.Subject
import com.example.data.local.Task
import com.example.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class StudyPlanState(
    val plans: List<StudyPlan> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = false
)

class StudyPlanViewModel(private val repository: StudyFlowRepository) : ViewModel() {

    val state: StateFlow<StudyPlanState> = combine(
        repository.getStudyPlans(),
        repository.getSubjects()
    ) { plans, subjects ->
        StudyPlanState(
            plans = plans,
            subjects = subjects
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudyPlanState(isLoading = true))

    fun addStudyPlan(plan: StudyPlan, onComplete: () -> Unit) {
        viewModelScope.launch {
            val planId = repository.insertStudyPlan(plan)
            generateScheduleForPlan(plan.copy(id = planId.toInt()))
            onComplete()
        }
    }

    private suspend fun generateScheduleForPlan(plan: StudyPlan) {
        // Algorithm to break down subjects and create tasks across the available days.
        val subjectIds = plan.subjectsInvolved.split(",").mapNotNull { it.toIntOrNull() }
        val subjects = subjectIds.mapNotNull { repository.getSubjectById(it) }
        if (subjects.isEmpty()) return

        val daysOfWeek = plan.availableDaysOfWeek.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        if (daysOfWeek.isEmpty()) return

        val targetDate = plan.endDate
        val calendar = Calendar.getInstance()
        var currentDate = plan.startDate
        
        val newTasks = mutableListOf<Task>()
        val maxMinutesPerDay = (plan.availableHoursPerDay * 60).toInt()

        // Very basic initial algorithm for Phase 4:
        // Distribute topics among the days until target date.
        var safetyCounter = 0
        var subjectIndex = 0
        var topicIndex = 0

        while (currentDate < targetDate && safetyCounter < 365) {
            calendar.timeInMillis = currentDate
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            if (daysOfWeek.contains(currentDayOfWeek)) {
                // Determine what to study today
                val currentSubject = subjects[subjectIndex]
                val topics = currentSubject.examContent.split(",").map { it.trim() }.filter { it.isNotBlank() }
                
                if (topics.isNotEmpty() && topicIndex < topics.size) {
                    val topic = topics[topicIndex]
                    
                    // Consider difficulty for time
                    val baseTime = maxMinutesPerDay / subjects.size.coerceAtLeast(1)
                    val difficultyMultiplier = 1f + (plan.difficulty - 3) * 0.1f // +/- 10% per diff level
                    val estimatedMinutes = (baseTime * difficultyMultiplier).toInt().coerceAtLeast(15)
                    
                    // Determine recommended method
                    val recommendedMethod = determineRecommendedMethod(
                        type = "study",
                        difficulty = plan.difficulty,
                        estimatedMinutes = estimatedMinutes,
                        goal = plan.goal
                    )

                    newTasks.add(
                        Task(
                            subjectId = currentSubject.id,
                            title = "${currentSubject.name}: $topic",
                            description = "Objetivo: ${plan.name}",
                            date = currentDate,
                            priority = plan.priority,
                            estimatedMinutes = estimatedMinutes,
                            type = "study",
                            origin = "normal",
                            difficulty = plan.difficulty,
                            energyLevel = plan.difficulty,
                            recommendedMethodId = recommendedMethod,
                            goalId = plan.id
                        )
                    )

                    // Add a revision task later
                    val revCalendar = Calendar.getInstance()
                    revCalendar.timeInMillis = currentDate
                    revCalendar.add(Calendar.DAY_OF_YEAR, 3) // Revise in 3 days
                    if (revCalendar.timeInMillis < targetDate) {
                         newTasks.add(
                            Task(
                                subjectId = currentSubject.id,
                                title = "Revisar: $topic",
                                description = "Revisão espaçada",
                                date = revCalendar.timeInMillis,
                                priority = plan.priority,
                                estimatedMinutes = estimatedMinutes / 2,
                                type = "revision",
                                origin = "normal",
                                difficulty = (plan.difficulty - 1).coerceAtLeast(1),
                                energyLevel = (plan.difficulty - 1).coerceAtLeast(1),
                                recommendedMethodId = "spaced_repetition",
                                goalId = plan.id
                            )
                        )
                    }

                    topicIndex++
                } else {
                    // Move to next subject
                    subjectIndex = (subjectIndex + 1) % subjects.size
                    topicIndex = 0
                }
            }

            // Move to next day
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            currentDate = calendar.timeInMillis
            safetyCounter++
        }

        if (newTasks.isNotEmpty()) {
            repository.insertTasks(newTasks)
        }
    }
    
    private fun determineRecommendedMethod(type: String, difficulty: Int, estimatedMinutes: Int, goal: String): String {
        if (type == "revision") return "spaced_repetition"
        if (estimatedMinutes > 120) return "52_17"
        if (goal.contains("Aprendizado", ignoreCase = true) || difficulty >= 4) return "feynman"
        if (goal.contains("ENEM", ignoreCase = true) || goal.contains("Vestibular", ignoreCase = true)) return "interleaving"
        return "pomodoro"
    }
}
