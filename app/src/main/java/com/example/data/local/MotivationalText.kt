package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "motivational_texts")
data class MotivationalText(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val themeId: Int,
    val title: String,
    val content: String,
    val source: String,
    val displayOrder: Int // 1: Contexto, 2: Outro ponto, 3: Dados, 4: Referência
)
