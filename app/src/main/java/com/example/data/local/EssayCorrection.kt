package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "essay_corrections")
data class EssayCorrection(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val submissionId: Int,
    val comp1: Int, // 0 to 200
    val comp2: Int,
    val comp3: Int,
    val comp4: Int,
    val comp5: Int,
    val totalScore: Int,
    val strengths: String,
    val weaknesses: String,
    val suggestions: String,
    val date: Long
)
