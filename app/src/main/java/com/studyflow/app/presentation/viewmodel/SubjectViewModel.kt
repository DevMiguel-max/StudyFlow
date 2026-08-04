package com.studyflow.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.local.Subject
import com.studyflow.app.data.local.Task
import com.studyflow.app.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class SubjectState(
    val subjects: List<Subject> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class SubjectViewModel(private val repository: StudyFlowRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val state: StateFlow<SubjectState> = combine(
        repository.getSubjects(),
        _searchQuery
    ) { subjects, query ->
        val filtered = if (query.isBlank()) subjects else subjects.filter {
            it.name.contains(query, ignoreCase = true) || it.professor.contains(query, ignoreCase = true)
        }
        SubjectState(subjects = filtered, searchQuery = query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubjectState(isLoading = true))

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addSubject(subject: Subject) {
        viewModelScope.launch {
            repository.insertSubject(subject)
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun addSubjectWithPlan(subject: Subject) {
        viewModelScope.launch {
            val id = repository.insertSubject(subject)
            generateStudyPlan(subject.copy(id = id.toInt()))
        }
    }

    fun updateSubjectWithPlan(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
            // Option 1: Delete all pending generated tasks and recreate
            // For simplicity, we just delete pending tasks of this subject
            val existingTasks = repository.getTasksForSubject(subject.id).first()
            existingTasks.filter { it.status == "pending" }.forEach { 
                repository.deleteTask(it)
            }
            generateStudyPlan(subject)
        }
    }

    private suspend fun generateStudyPlan(subject: Subject) {
        val topics = subject.examContent.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (topics.isEmpty()) return

        val daysOfWeek = subject.availableDaysOfWeek.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        if (daysOfWeek.isEmpty()) return

        val targetDate = subject.examDate ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000) // Default 30 days
        
        val calendar = Calendar.getInstance()
        var currentDate = System.currentTimeMillis()
        
        val newTasks = mutableListOf<Task>()
        
        // Simple algorithm: assign topics to available days
        // We calculate max minutes per day based on availableHoursPerDay
        val maxMinutesPerDay = (subject.availableHoursPerDay * 60).toInt()
        val estimatedMinutesPerTopic = maxMinutesPerDay / (if (subject.difficulty > 3) 1 else 2).coerceAtLeast(1)

        var topicIndex = 0
        var safetyCounter = 0
        
        while (topicIndex < topics.size && currentDate < targetDate && safetyCounter < 365) {
            calendar.timeInMillis = currentDate
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            
            if (daysOfWeek.contains(currentDayOfWeek)) {
                // Add study task
                val topic = topics[topicIndex]
                newTasks.add(
                    Task(
                        subjectId = subject.id,
                        title = "Estudar: $topic",
                        description = "Conteúdo gerado automaticamente para o objetivo: ${subject.studyGoal}",
                        date = currentDate,
                        priority = subject.difficulty,
                        estimatedMinutes = estimatedMinutesPerTopic,
                        type = "study"
                    )
                )
                
                // Add revision task 1 week later
                val revCalendar = Calendar.getInstance()
                revCalendar.timeInMillis = currentDate
                revCalendar.add(Calendar.DAY_OF_YEAR, 7)
                
                // Adjust revision to an available day
                var revSafety = 0
                while (!daysOfWeek.contains(revCalendar.get(Calendar.DAY_OF_WEEK)) && revSafety < 7) {
                    revCalendar.add(Calendar.DAY_OF_YEAR, 1)
                    revSafety++
                }
                
                if (revCalendar.timeInMillis < targetDate) {
                    newTasks.add(
                        Task(
                            subjectId = subject.id,
                            title = "Revisar: $topic",
                            description = "Revisão espaçada gerada pelo StudyFlow",
                            date = revCalendar.timeInMillis,
                            priority = subject.difficulty,
                            estimatedMinutes = estimatedMinutesPerTopic / 2,
                            type = "revision"
                        )
                    )
                }
                
                topicIndex++
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

    fun deleteSubject(subject: Subject, deleteTasks: Boolean, moveToSubjectId: Int?) {
        viewModelScope.launch {
            if (deleteTasks) {
                repository.deleteTasksBySubject(subject.id)
            } else if (moveToSubjectId != null) {
                repository.moveTasksToSubject(subject.id, moveToSubjectId)
            }
            repository.deleteSubject(subject)
        }
    }
}
