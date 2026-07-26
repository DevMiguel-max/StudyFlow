package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "essay_submissions")
data class EssaySubmission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val themeId: Int,
    val text: String,
    val date: Long,
    val timeSpentSeconds: Int,
    val status: String, // "draft", "completed", "sent", "graded"
    val wordCount: Int = 0,
    val charCount: Int = 0
)
