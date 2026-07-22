package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "essay_themes")
data class EssayTheme(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // Tecnologia, Educação, Saúde, Meio ambiente, Sociedade, Cultura, Direitos humanos, Atualidades.
    val difficulty: String, // Fácil, Médio, Difícil
    val examType: String, // ENEM, Vestibular
    val createdAt: Long = System.currentTimeMillis()
)
