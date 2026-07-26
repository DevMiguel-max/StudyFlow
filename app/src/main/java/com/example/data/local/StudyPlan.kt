package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_plans")
data class StudyPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val name: String,
    val goal: String, // e.g. "Melhorar em uma matéria", "Preparar para ENEM"
    val subjectsInvolved: String, // comma-separated subject IDs
    val startDate: Long,
    val endDate: Long,
    val availableHoursPerDay: Float,
    val availableDaysOfWeek: String, // e.g. "1,2,3,4,5"
    val difficulty: Int, // 1 to 5
    val priority: Int, // 1 to 5
    val progress: Float = 0f // 0.0 to 1.0
)
