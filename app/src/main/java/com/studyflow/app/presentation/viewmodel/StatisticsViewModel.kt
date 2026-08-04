package com.studyflow.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.local.*
import com.studyflow.app.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

data class StatisticsState(
    // Dashboard Principal
    val timeStudiedToday: Int = 0,
    val timeStudiedWeek: Int = 0,
    val timeStudiedMonth: Int = 0,
    val timeStudiedTotal: Int = 0,
    val currentGoal: StudyGoal? = null,
    val daysToExam: Int? = null,
    val studyStreak: Int = 0,
    val upcomingReviewsCount: Int = 0,
    val upcomingTasksCount: Int = 0,
    
    // Matérias
    val subjectStats: List<SubjectStat> = emptyList(),
    
    // Técnicas
    val methodStats: List<MethodStat> = emptyList(),
    
    // Redação
    val essayStats: EssayStat? = null,
    
    // Simulados
    val simulationStats: SimulationStat? = null,
    
    // Objetivos
    val goalStats: List<GoalStat> = emptyList(),
    
    // Insights
    val insights: List<String> = emptyList(),
    val userProfile: com.studyflow.app.data.local.UserProfile? = null
)

data class SubjectStat(
    val subject: Subject,
    val timeStudied: Int,
    val sessionsCount: Int,
    val reviewsCount: Int,
    val tasksCompleted: Int,
    val performance: Float,
    val lastStudyDate: Long?
)

data class MethodStat(
    val methodId: String,
    val timeStudied: Int,
    val sessionsCount: Int,
    val isMostUsed: Boolean = false,
    val isMostEfficient: Boolean = false
)

data class EssayStat(
    val totalCount: Int,
    val averageScore: Float,
    val averageTime: Int,
    val c1Avg: Float,
    val c2Avg: Float,
    val c3Avg: Float,
    val c4Avg: Float,
    val c5Avg: Float,
    val history: List<EssaySubmission>
)

data class SimulationStat(
    val totalCount: Int,
    val averageScore: Float,
    val bestScore: Float,
    val lastScore: Float,
    val history: List<Simulation>
)

data class GoalStat(
    val goal: StudyGoal,
    val progressPercent: Float,
    val daysLeft: Int?,
    val timeInvested: Int
)

class StatisticsViewModel(private val repository: StudyFlowRepository) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllStudySessions(),
        repository.getUserProfile(),
                repository.getAllStudyGoals(),
                repository.getSubjects(),
                repository.getTasks(),
                repository.getAllReviewSchedules(),
                repository.getAllEssaySubmissions(),
                repository.getAllSimulations(),
                repository.getAllEssayCorrections()
                        ) { args: Array<Any?> ->
                val sessions = args[0] as List<StudySession>
                val profile = args[1] as? com.studyflow.app.data.local.UserProfile
                val goals = args[2] as List<StudyGoal>
                val subjects = args[3] as List<Subject>
                val tasks = args[4] as List<Task>
                val reviews = args[5] as List<ReviewSchedule>
                val essays = args[6] as List<EssaySubmission>
                val simulations = args[7] as List<Simulation>
                val corrections = args[8] as List<EssayCorrection>
                val newState = computeStats(sessions, goals, subjects, tasks, reviews, essays, simulations, corrections)
                newState.copy(userProfile = profile)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private fun computeStats(
        sessions: List<StudySession>,
        goals: List<StudyGoal>,
        subjects: List<Subject>,
        tasks: List<Task>,
        reviews: List<ReviewSchedule>,
        essays: List<EssaySubmission>,
        simulations: List<Simulation>,
        corrections: List<EssayCorrection>
    ): StatisticsState {
        val now = System.currentTimeMillis()
        val todayStart = getStartOfDay(now)
        val weekStart = getStartOfWeek(now)
        val monthStart = getStartOfMonth(now)

        val completedSessions = sessions.filter { it.status == "completed" }
        
        // Time studied
        val timeToday = completedSessions.filter { it.date >= todayStart }.sumOf { it.durationMinutes }
        val timeWeek = completedSessions.filter { it.date >= weekStart }.sumOf { it.durationMinutes }
        val timeMonth = completedSessions.filter { it.date >= monthStart }.sumOf { it.durationMinutes }
        val timeTotal = completedSessions.sumOf { it.durationMinutes }

        // Dashboard
        val activeGoal = goals.filter { it.status == "active" }.minByOrNull { it.date ?: Long.MAX_VALUE }
        val daysToExam = activeGoal?.date?.let { 
            val diff = it - now
            if (diff > 0) TimeUnit.MILLISECONDS.toDays(diff).toInt() else 0 
        }
        
        val streak = calculateStreak(completedSessions)
        val upcomingReviews = reviews.count { it.reviewDate >= todayStart && !it.isCompleted }
        val upcomingTasks = tasks.count { it.status != "completed" && it.date != null && it.date >= todayStart }

        // Subject Stats
        val subjectStatsList = subjects.map { subject ->
            val subjSessions = completedSessions.filter { it.subjectId == subject.id }
            val subjReviews = reviews.filter { it.subjectId == subject.id && it.isCompleted }
            val subjTasks = tasks.filter { it.subjectId == subject.id && it.status == "completed" }
            val avgPerf = subjSessions.mapNotNull { it.perceivedPerformance }.map { it.toFloat() * 20f }.average()
            SubjectStat(
                subject = subject,
                timeStudied = subjSessions.sumOf { it.durationMinutes },
                sessionsCount = subjSessions.size,
                reviewsCount = subjReviews.size,
                tasksCompleted = subjTasks.size,
                performance = if (avgPerf.isNaN()) 0f else avgPerf.toFloat(),
                lastStudyDate = subjSessions.maxOfOrNull { it.date }
            )
        }.sortedByDescending { it.timeStudied }

        // Method Stats
        val methodGroups = completedSessions.groupBy { it.techniqueUsed }
        val methodStatsList = methodGroups.map { (methodId, sess) ->
            MethodStat(
                methodId = methodId ?: "unknown",
                timeStudied = sess.sumOf { it.durationMinutes },
                sessionsCount = sess.size,
                isMostUsed = false,
                isMostEfficient = false
            )
        }.sortedByDescending { it.timeStudied }
        
        val maxTimeMethod = methodStatsList.maxByOrNull { it.timeStudied }?.methodId
        val maxEffMethod = methodStatsList.maxByOrNull { it.sessionsCount }?.methodId 
        
        val finalMethodStats = methodStatsList.map { 
            it.copy(
                isMostUsed = it.methodId == maxTimeMethod,
                isMostEfficient = it.methodId == maxEffMethod
            )
        }

        // Essay Stats
        val gradedSubmissions = essays.filter { it.status == "graded" }
        val essayStats = if (essays.isNotEmpty()) {
            val validCorrections = corrections.filter { corr -> gradedSubmissions.any { it.id == corr.submissionId } }
            EssayStat(
                totalCount = essays.size,
                averageScore = if (validCorrections.isEmpty()) 0f else validCorrections.map { it.totalScore }.average().toFloat(),
                averageTime = if (essays.isEmpty()) 0 else essays.map { it.timeSpentSeconds / 60 }.average().toInt(),
                c1Avg = if (validCorrections.isEmpty()) 0f else validCorrections.map { it.comp1 }.average().toFloat(),
                c2Avg = if (validCorrections.isEmpty()) 0f else validCorrections.map { it.comp2 }.average().toFloat(),
                c3Avg = if (validCorrections.isEmpty()) 0f else validCorrections.map { it.comp3 }.average().toFloat(),
                c4Avg = if (validCorrections.isEmpty()) 0f else validCorrections.map { it.comp4 }.average().toFloat(),
                c5Avg = if (validCorrections.isEmpty()) 0f else validCorrections.map { it.comp5 }.average().toFloat(),
                history = essays.sortedByDescending { it.date }
            )
        } else null

        // Simulation Stats
        val simulationStats = if (simulations.isNotEmpty()) {
            val avgScore = simulations.map { it.score.toDouble() }.average()
            SimulationStat(
                totalCount = simulations.size,
                averageScore = if (avgScore.isNaN()) 0f else avgScore.toFloat(),
                bestScore = simulations.maxOfOrNull { it.score } ?: 0f,
                lastScore = simulations.maxByOrNull { it.date }?.score ?: 0f,
                history = simulations.sortedByDescending { it.date }
            )
        } else null

        // Goal Stats
        val goalStatsList = goals.map { goal ->
            val daysLeft = goal.date?.let { 
                val diff = it - now
                if (diff > 0) TimeUnit.MILLISECONDS.toDays(diff).toInt() else 0 
            }
            GoalStat(
                goal = goal,
                progressPercent = (completedSessions.size * 5f).coerceAtMost(100f), // Mock
                daysLeft = daysLeft,
                timeInvested = completedSessions.sumOf { it.durationMinutes } // Mock
            )
        }

        // Insights
        val generatedInsights = mutableListOf<String>()
        val mostStudiedSubject = subjectStatsList.maxByOrNull { it.timeStudied }
        if (mostStudiedSubject != null && mostStudiedSubject.timeStudied > 0) {
            generatedInsights.add("Você focou bastante em ${mostStudiedSubject.subject.name} recentemente.")
        }
        val leastStudiedSubject = subjectStatsList.filter { it.timeStudied > 0 }.minByOrNull { it.timeStudied }
        if (leastStudiedSubject != null) {
            generatedInsights.add("Sua matéria menos estudada foi ${leastStudiedSubject.subject.name}.")
        }
        val unreviewedSubject = subjectStatsList.firstOrNull { it.lastStudyDate != null && (now - it.lastStudyDate) > 5 * 24 * 60 * 60 * 1000L }
        if (unreviewedSubject != null) {
            generatedInsights.add("Faz mais de 5 dias que você não revisa ${unreviewedSubject.subject.name}.")
        }
        if (tasks.isNotEmpty() && tasks.all { it.status == "completed" }) {
            generatedInsights.add("Incrível! Você concluiu todas as suas tarefas.")
        } else if (tasks.count { it.status == "completed" && it.date != null && it.date >= todayStart } > 0) {
            generatedInsights.add("Bom progresso nas tarefas de hoje!")
        }
        if (timeWeek > timeMonth/4 + 60) {
             generatedInsights.add("Seu tempo de estudo aumentou nesta semana!")
        }

        return StatisticsState(
            timeStudiedToday = timeToday,
            timeStudiedWeek = timeWeek,
            timeStudiedMonth = timeMonth,
            timeStudiedTotal = timeTotal,
            currentGoal = activeGoal,
            daysToExam = daysToExam,
            studyStreak = streak,
            upcomingReviewsCount = upcomingReviews,
            upcomingTasksCount = upcomingTasks,
            subjectStats = subjectStatsList,
            methodStats = finalMethodStats,
            essayStats = essayStats,
            simulationStats = simulationStats,
            goalStats = goalStatsList,
            insights = generatedInsights
        )
    }

    private fun calculateStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0
        val days = sessions.map { getStartOfDay(it.date) }.distinct().sortedDescending()
        if (days.isEmpty()) return 0
        
        val today = getStartOfDay(System.currentTimeMillis())
        var streak = 0
        var currentCheckDay = today

        if (days[0] == today) {
            streak = 1
            currentCheckDay = today - 24 * 60 * 60 * 1000L
        } else if (days[0] == today - 24 * 60 * 60 * 1000L) {
            streak = 0
            currentCheckDay = today - 24 * 60 * 60 * 1000L
        } else {
            return 0
        }

        for (i in (if (streak == 1) 1 else 0) until days.size) {
            if (days[i] == currentCheckDay) {
                streak++
                currentCheckDay -= 24 * 60 * 60 * 1000L
            } else {
                break
            }
        }
        return streak
    }

    private fun getStartOfDay(time: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getStartOfWeek(time: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun getStartOfMonth(time: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
