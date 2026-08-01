package com.studyflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mentor_profile")
data class MentorProfile(
    @PrimaryKey val id: Int = 1,
    val mainGoal: String = "",
    val examType: String = "", // Concurso, vestibular, ENEM
    val examDate: Long = 0L,
    val hoursPerDay: Int = 4,
    val availableDays: String = "1,2,3,4,5", // e.g. "1,2,3,4,5"
    val preferredTime: String = "Manhã",
    val favoriteSubjects: String = "",
    val difficultSubjects: String = "",
    val preferredStudyMethod: String = "Pomodoro",
    val learningPace: String = "Médio",
    val coachModeEnabled: Boolean = true,
    val mentorEnabled: Boolean = true
)

@Entity(tableName = "mentor_recommendation")
data class MentorRecommendation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val reason: String,
    val type: String, // "Schedule", "Review", "Warning", "Motivation"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isAccepted: Boolean? = null // null means not a proposal or not answered
)

@Entity(tableName = "smart_mission")
data class SmartMission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val type: String, // "Review", "Flashcard", "Question", "Pomodoro"
    val targetAmount: Int = 0,
    val currentAmount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "mentor_history")
data class MentorHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String, // "RecommendationAccepted", "ScheduleChanged", "AnalysisRun"
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
