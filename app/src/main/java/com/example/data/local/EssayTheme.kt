package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "essay_themes")
data class EssayTheme(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val theme: String,
    val category: String,
    val difficulty: Int,
    val examType: String
)
