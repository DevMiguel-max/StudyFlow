package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_cache")
data class AICacheEntity(
    @PrimaryKey val cacheKey: String,
    val content: String,
    val timestamp: Long,
    val type: String // e.g., "summary", "flashcards", "questions", "general"
)
