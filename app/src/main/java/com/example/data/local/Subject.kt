package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val name: String = "",
    val color: String = "",
    val icon: String = "",
    val professor: String = "",
    val description: String = "",
    val difficulty: Int = 1,
    val examDate: Long? = null,
    val weeklyHoursTarget: Int = 0,
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    
    // Novas propriedades para o Plano de Estudos
    val examContent: String = "",
    val availableHoursPerDay: Float = 2f,
    val availableDaysOfWeek: String = "2,3,4,5,6", // 1=Sunday, 2=Monday...
    val studyGoal: String = "Aprendizado"
)
