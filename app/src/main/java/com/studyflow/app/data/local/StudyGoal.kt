package com.studyflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_goals")
data class StudyGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val type: String, // "Escola", "ENEM", "Vestibular", "Concurso", "Faculdade", "Certificação", "Idioma", "Personalizado"
    val title: String,
    val description: String? = null,
    val institution: String? = null, // Faculdade, Órgão do Concurso, etc.
    val role: String? = null, // Cargo (Concurso)
    val course: String? = null, // Curso (Faculdade/Vestibular)
    val certification: String? = null,
    val language: String? = null,
    val examBoard: String? = null, // Banca (Cebraspe, FGV, FCC, etc.)
    val date: Long? = null,
    val targetScore: Float? = null,
    val priority: Int = 2, // 1: Baixa, 2: Média, 3: Alta
    val status: String = "active" // "active", "completed", "archived"
)
