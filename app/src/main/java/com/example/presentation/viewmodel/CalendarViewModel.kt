package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class CalendarState(
    val tasks: List<Task> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val sessions: List<StudySession> = emptyList(),
    val reviews: List<ReviewSchedule> = emptyList(),
    val essays: List<EssaySubmission> = emptyList(),
    val simulations: List<Simulation> = emptyList(),
    val goals: List<StudyGoal> = emptyList(),
    val selectedDateMillis: Long = System.currentTimeMillis(),
    
    val eventsForSelectedDate: List<CalendarEvent> = emptyList(),
    val daysWithEvents: Map<Long, List<EventType>> = emptyMap()
)

enum class EventType { TASK, SESSION, REVIEW, ESSAY, SIMULATION, EXAM, GOAL }

data class CalendarEvent(
    val id: String,
    val title: String,
    val type: EventType,
    val timeMillis: Long,
    val isCompleted: Boolean = false
)

class CalendarViewModel(private val repository: StudyFlowRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    
    val state: StateFlow<CalendarState> = combine(
        repository.getTasks(),
        repository.getSubjects(),
        repository.getAllStudySessions(),
        repository.getAllReviewSchedules(),
        repository.getAllEssaySubmissions(),
        repository.getAllSimulations(),
        repository.getAllStudyGoals(),
        _selectedDate
    ) { args: Array<Any> ->
        val tasks = args[0] as List<Task>
        val subjects = args[1] as List<Subject>
        val sessions = args[2] as List<StudySession>
        val reviews = args[3] as List<ReviewSchedule>
        val essays = args[4] as List<EssaySubmission>
        val simulations = args[5] as List<Simulation>
        val goals = args[6] as List<StudyGoal>
        val selectedDate = args[7] as Long
        
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate
        val startOfDay = getStartOfDay(selectedDate)
        val endOfDay = getEndOfDay(selectedDate)

        val allEvents = mutableListOf<CalendarEvent>()
        
        tasks.forEach { 
            if (it.date != null) allEvents.add(CalendarEvent("task_${it.id}", "Tarefa: ${it.title}", EventType.TASK, it.date, it.status == "completed"))
        }
        sessions.forEach {
            allEvents.add(CalendarEvent("session_${it.id}", "Sessão de Estudo", EventType.SESSION, it.date, it.status == "completed"))
        }
        reviews.forEach {
            allEvents.add(CalendarEvent("review_${it.id}", "Revisão: ${it.topic}", EventType.REVIEW, it.reviewDate, it.isCompleted))
        }
        essays.forEach {
            allEvents.add(CalendarEvent("essay_${it.id}", "Redação", EventType.ESSAY, it.date, it.status == "completed" || it.status == "graded"))
        }
        simulations.forEach {
            allEvents.add(CalendarEvent("sim_${it.id}", "Simulado: ${it.name}", EventType.SIMULATION, it.date, true))
        }
        subjects.forEach {
            if (it.examDate != null) allEvents.add(CalendarEvent("exam_${it.id}", "Prova: ${it.name}", EventType.EXAM, it.examDate, false))
        }
        goals.forEach {
            if (it.date != null) allEvents.add(CalendarEvent("goal_${it.id}", "Objetivo: ${it.title}", EventType.GOAL, it.date, false))
        }

        val eventsForSelectedDate = allEvents.filter { it.timeMillis in startOfDay..endOfDay }

        val daysWithEvents = allEvents.groupBy { getStartOfDay(it.timeMillis) }
            .mapValues { entry -> entry.value.map { it.type }.distinct() }

        CalendarState(
            tasks = tasks,
            subjects = subjects,
            sessions = sessions,
            reviews = reviews,
            essays = essays,
            simulations = simulations,
            goals = goals,
            selectedDateMillis = selectedDate,
            eventsForSelectedDate = eventsForSelectedDate,
            daysWithEvents = daysWithEvents
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarState())

    fun selectDate(dateMillis: Long) {
        _selectedDate.value = dateMillis
    }
    
    private fun getStartOfDay(time: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    private fun getEndOfDay(time: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
    }
}
