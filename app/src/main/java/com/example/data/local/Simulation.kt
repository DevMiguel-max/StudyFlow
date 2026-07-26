package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "simulations")
data class Simulation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val name: String,
    val examType: String, // ENEM, Vestibular, etc.
    val date: Long,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val score: Float
)
