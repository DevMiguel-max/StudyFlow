package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: String, // "DAILY", "WEEKLY"
    val requiredAmount: Int,
    val currentAmount: Int = 0,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val expiresAt: Long
)
