package com.studyflow.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val subjectId: Int,
    val title: String,
    val description: String = "",
    val date: Long? = null,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val status: String = "pending", // pending, in_progress, completed
    val priority: Int = 1,
    val estimatedMinutes: Int = 0,
    val type: String = "study", // study (Aula), exercise (Exercícios), revision (Revisão), essay (Redação), simulation (Simulado)
    
    // New fields for Phase 4+
    val origin: String = "normal", // normal, enem, vestibular
    val difficulty: Int = 1, // 1 to 5
    val energyLevel: Int = 1, // 1 (Low) to 5 (High)
    val recommendedMethodId: String? = null,
    val goalId: Int? = null
)
