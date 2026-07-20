package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.Subject
import com.example.data.local.Task
import com.example.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TaskFilterState(
    val subjectId: Int? = null,
    val priority: Int? = null,
    val completed: Boolean? = null
)

data class TaskState(
    val tasks: List<Task> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val searchQuery: String = "",
    val filterState: TaskFilterState = TaskFilterState(),
    val isLoading: Boolean = false
)

class TaskViewModel(private val repository: StudyFlowRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filterState = MutableStateFlow(TaskFilterState())

    val state: StateFlow<TaskState> = combine(
        repository.getTasks(),
        repository.getSubjects(),
        _searchQuery,
        _filterState
    ) { tasks, subjects, query, filters ->
        var filtered = tasks

        if (query.isNotBlank()) {
            filtered = filtered.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
        }
        if (filters.subjectId != null) {
            filtered = filtered.filter { it.subjectId == filters.subjectId }
        }
        if (filters.priority != null) {
            filtered = filtered.filter { it.priority == filters.priority }
        }
        if (filters.completed != null) {
            filtered = filtered.filter { (it.status == "completed") == filters.completed }
        }

        TaskState(
            tasks = filtered,
            subjects = subjects,
            searchQuery = query,
            filterState = filters
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskState(isLoading = true))

    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }
    fun onFilterSubjectChanged(subjectId: Int?) { _filterState.value = _filterState.value.copy(subjectId = subjectId) }
    fun onFilterPriorityChanged(priority: Int?) { _filterState.value = _filterState.value.copy(priority = priority) }
    fun onFilterCompletedChanged(completed: Boolean?) { _filterState.value = _filterState.value.copy(completed = completed) }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }
    
    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.status == "completed") "pending" else "completed"
            repository.updateTask(task.copy(status = newStatus))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}
