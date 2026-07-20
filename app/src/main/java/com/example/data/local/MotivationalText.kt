package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "motivational_texts")
data class MotivationalText(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val displayOrder: Int,
    val source: String
)
