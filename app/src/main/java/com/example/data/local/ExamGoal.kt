package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_goals")
data class ExamGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "ENEM", "Vestibular", "Escolar"
    val name: String, // e.g., "ENEM 2024", "FUVEST", "Prova de Matemática"
    val institution: String? = null,
    val course: String? = null,
    val examDate: Long,
    val targetScore: Float? = null,
    val phases: Int = 1,
    val hardestSubjectsIds: String = "", // Comma-separated IDs
    val content: String? = null
)
