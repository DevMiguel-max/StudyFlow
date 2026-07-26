package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_schedules")
data class ReviewSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val subjectId: Int,
    val topic: String,
    val reviewDate: Long,
    val isCompleted: Boolean = false,
    val intervalDays: Int // 1, 3, 7, 15, 30
)
