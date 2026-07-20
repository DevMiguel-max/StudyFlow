package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "simulations")
data class Simulation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examType: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val date: Long
)
