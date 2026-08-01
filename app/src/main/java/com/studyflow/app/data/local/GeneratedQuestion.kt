package com.studyflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_questions")
data class GeneratedQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val statement: String,
    val options: String, // JSON list of options if applicable
    val correctAnswer: String,
    val explanation: String,
    val difficulty: String,
    val subject: String,
    val topic: String,
    val source: String? = null,
    val isAnswered: Boolean = false,
    val wasCorrect: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
