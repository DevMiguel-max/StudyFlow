package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_notes")
data class StudyNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val title: String,
    val type: String, // "cornell", "feynman", "blurting", "standard"
    val contentMain: String, // Main notes / My explanation / Blurting content
    val contentSecondary: String? = null, // Keywords (Cornell) / Where I struggled (Feynman)
    val summary: String? = null, // Summary (Cornell) / What I learned (Feynman)
    val date: Long
)
