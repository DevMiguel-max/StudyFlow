package com.studyflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val question: String,
    val answer: String,
    val box: Int = 1, // 1 to 3 for Leitner
    val nextReviewDate: Long,
    val category: String? = null,
    val topic: String? = null,
    val difficulty: String? = null,
    val source: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
