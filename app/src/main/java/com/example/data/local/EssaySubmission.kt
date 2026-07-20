package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "essay_submissions")
data class EssaySubmission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val themeId: Int,
    val writtenText: String,
    val date: Long,
    val timeSpentMinutes: Int,
    val futureGrade: Float? = null // Future AI grading
)
