package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val updatedAt: Long = System.currentTimeMillis(),
    val xp: Int = 0,
    val level: Int = 1,
    val streakDays: Int = 0,
    val maxStreakDays: Int = 0,
    val lastStudyDate: Long = 0L,
    val avatarIcon: String = "🧑‍🎓",
    val name: String = "Estudante",
    val email: String = "",
    val mainGoal: String = "Aprovação",
    val avatarColor: String = "0xFFE0E7FF",
    val gamificationEnabled: Boolean = true,
    val soundsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)
