package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val taskId: Int?,
    val techniqueUsed: String, // e.g., "pomodoro", "feynman"
    val date: Long,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val status: String, // "completed", "interrupted"
    val notes: String? = null,
    val perceivedPerformance: Int? = null // 1 to 5
)
