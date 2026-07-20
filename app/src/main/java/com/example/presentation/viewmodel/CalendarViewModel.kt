package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.Subject
import com.example.data.local.Task
import com.example.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class CalendarState(
    val tasks: List<Task> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val tasksForSelectedDate: List<Task> = emptyList(),
    val examsForSelectedDate: List<Subject> = emptyList()
)

class CalendarViewModel(private val repository: StudyFlowRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())

    val state: StateFlow<CalendarState> = combine(
        repository.getTasks(),
        repository.getSubjects(),
        _selectedDate
    ) { tasks, subjects, selectedDate ->
        
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate
        val startOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val tasksForSelectedDate = tasks.filter {
            it.date != null && it.date in startOfDay..endOfDay
        }
        
        val examsForSelectedDate = subjects.filter {
            it.examDate != null && it.examDate in startOfDay..endOfDay
        }

        CalendarState(
            tasks = tasks,
            subjects = subjects,
            selectedDateMillis = selectedDate,
            tasksForSelectedDate = tasksForSelectedDate,
            examsForSelectedDate = examsForSelectedDate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarState())

    fun selectDate(dateMillis: Long) {
        _selectedDate.value = dateMillis
    }
}
