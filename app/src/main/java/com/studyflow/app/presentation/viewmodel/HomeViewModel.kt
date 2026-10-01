package com.studyflow.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.local.StudyPlan
import com.studyflow.app.data.local.Subject
import com.studyflow.app.data.local.Task
import com.studyflow.app.domain.repository.StudyFlowRepository
import com.studyflow.app.data.local.StudyMethodsData
import com.studyflow.app.data.local.StudyMethodModel
import com.studyflow.app.domain.ai.StudyTip
import com.studyflow.app.domain.ai.StudyTipService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeState(
    val subjectsCount: Int = 0,
    val tasksCount: Int = 0,
    val upcomingExamDays: Int? = null,
    val nextTask: Task? = null,
    val estimatedTimeTodayMinutes: Int = 0,
    val totalEstimatedTimeMinutes: Int = 0,
    val recentSubjects: List<Subject> = emptyList(),
    val todaysTasks: List<Task> = emptyList(),
    val nextRevision: Task? = null,
    val recommendedMethod: StudyMethodModel? = null,
    val activePlans: List<StudyPlan> = emptyList(),
    val timeStudiedTodayMinutes: Int = 0,
    val lastTechniqueUsed: StudyMethodModel? = null,
    val totalSessionsCompleted: Int = 0,
    val userProfile: com.studyflow.app.data.local.UserProfile? = null,
    val studyTip: StudyTip? = null,
    val isTipLoading: Boolean = false
)

class HomeViewModel(
    private val repository: StudyFlowRepository,
    private val studyTipService: StudyTipService = StudyTipService()
) : ViewModel() {

    private val _tipState = MutableStateFlow<Pair<StudyTip?, Boolean>>(Pair(null, true))

    init {
        loadDailyTip(forceRefresh = false)
    }

    fun loadDailyTip(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _tipState.value = _tipState.value.copy(second = true)
            val tip = studyTipService.getDailyTip(forceRefresh)
            _tipState.value = Pair(tip, false)
        }
    }

    private val baseState = combine(
        repository.getSubjects(),
        repository.getTasks(),
        repository.getStudyPlans(),
        repository.getAllStudySessions(),
        repository.getUserProfile()
    ) { subjects, tasks, plans, sessions, profile ->
        val pendingTasks = tasks.filter { it.status != "completed" }
        
        // Time calculations
        val totalTime = pendingTasks.sumOf { it.estimatedMinutes }
        
        // Today calculations
        val calendar = Calendar.getInstance()
        val todayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
        
        val todaysTasks = pendingTasks.filter { it.date != null && it.date in todayStart..todayEnd }
        val estimatedTimeToday = todaysTasks.sumOf { it.estimatedMinutes }
        
        val nextTask = todaysTasks.minByOrNull { it.date ?: Long.MAX_VALUE } ?: pendingTasks.minByOrNull { it.date ?: Long.MAX_VALUE }
        
        val nextRevision = pendingTasks.filter { it.type == "revision" }.minByOrNull { it.date ?: Long.MAX_VALUE }

        val upcomingExam = subjects.filter { it.examDate != null && it.examDate > System.currentTimeMillis() }
            .minByOrNull { it.examDate!! }
        val daysToExam = upcomingExam?.examDate?.let {
            ((it - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
        }
        
        // Intelligent Recommendation Logic
        val methodId = nextTask?.recommendedMethodId
        var recommendedMethod = if (methodId != null) {
            StudyMethodsData.methods.find { it.id == methodId }
        } else {
            determineRecommendedMethod(nextTask, subjects.find { it.id == nextTask?.subjectId })
        }
        
        val todaysSessions = sessions.filter { it.date in todayStart..todayEnd && it.status == "completed" }
        val timeStudiedToday = todaysSessions.sumOf { it.durationMinutes }
        val lastTechStr = sessions.maxByOrNull { it.date }?.techniqueUsed
        val lastTech = StudyMethodsData.methods.find { it.id == lastTechStr }

        HomeState(
            subjectsCount = subjects.size,
            tasksCount = tasks.size,
            upcomingExamDays = daysToExam,
            nextTask = nextTask,
            estimatedTimeTodayMinutes = estimatedTimeToday,
            totalEstimatedTimeMinutes = totalTime,
            recentSubjects = subjects.take(2),
            todaysTasks = todaysTasks.take(3),
            nextRevision = nextRevision,
            recommendedMethod = recommendedMethod ?: StudyMethodsData.methods.first(),
            activePlans = plans,
            timeStudiedTodayMinutes = timeStudiedToday,
            lastTechniqueUsed = lastTech,
            totalSessionsCompleted = sessions.count { it.status == "completed" },
            userProfile = profile
        )
    }

    val state: StateFlow<HomeState> = combine(baseState, _tipState) { base, tipInfo ->
        base.copy(
            studyTip = tipInfo.first,
            isTipLoading = tipInfo.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())
    
    private fun determineRecommendedMethod(nextTask: Task?, subject: Subject?): StudyMethodModel? {
        if (nextTask == null) return null
        
        return when {
            nextTask.type == "revision" -> StudyMethodsData.methods.find { it.id == "spaced_repetition" }
            nextTask.estimatedMinutes > 120 -> StudyMethodsData.methods.find { it.id == "52_17" }
            subject?.studyGoal == "Aprendizado" -> StudyMethodsData.methods.find { it.id == "feynman" }
            subject?.difficulty ?: 1 >= 4 -> StudyMethodsData.methods.find { it.id == "cornell" }
            else -> StudyMethodsData.methods.find { it.id == "pomodoro" }
        }
    }
}
