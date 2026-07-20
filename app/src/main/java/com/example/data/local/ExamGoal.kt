package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_goals")
data class ExamGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examType: String, // ENEM, Vestibular
    val institution: String,
    val course: String,
    val examDate: Long,
    val targetScore: Float
)
