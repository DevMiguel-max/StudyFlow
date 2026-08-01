package com.studyflow.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val tier: String, // "Bronze", "Prata", "Ouro", "Diamante"
    val category: String,
    val progress: Int = 0,
    val maxProgress: Int = 1,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)
