package com.example.domain.manager

import com.example.data.local.Achievement
import com.example.data.local.Challenge
import com.example.data.local.UserProfile
import com.example.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class GamificationManager(private val repository: StudyFlowRepository) {

    private val xpToLevelUp = listOf(
        0, 100, 250, 500, 850, 1300, 1850, 2500, 3250, 4100, 5050
    )

    fun getRequiredXpForLevel(level: Int): Int {
        if (level <= 0) return 0
        if (level <= xpToLevelUp.size) {
            return xpToLevelUp[level - 1]
        }
        val last = xpToLevelUp.last()
        val diff = level - xpToLevelUp.size
        return last + (diff * 1500)
    }

    suspend fun addXp(amount: Int) {
        val profile = repository.getUserProfile().firstOrNull() ?: return
        if (!profile.gamificationEnabled) return
        
        var newXp = profile.xp + amount
        var newLevel = profile.level
        
        while (newXp >= getRequiredXpForLevel(newLevel + 1)) {
            newLevel++
        }

        repository.updateUserProfile(profile.copy(xp = newXp, level = newLevel))
    }

    suspend fun updateStreak() {
        val profile = repository.getUserProfile().firstOrNull() ?: return
        if (!profile.gamificationEnabled) return

        val now = Calendar.getInstance()
        val lastDate = Calendar.getInstance().apply { timeInMillis = profile.lastStudyDate }
        
        var streak = profile.streakDays
        var maxStreak = profile.maxStreakDays
        
        if (profile.lastStudyDate == 0L) {
            streak = 1
            maxStreak = 1
        } else {
            val sameDay = now.get(Calendar.YEAR) == lastDate.get(Calendar.YEAR) &&
                          now.get(Calendar.DAY_OF_YEAR) == lastDate.get(Calendar.DAY_OF_YEAR)
            
            if (!sameDay) {
                lastDate.add(Calendar.DAY_OF_YEAR, 1)
                val isConsecutive = now.get(Calendar.YEAR) == lastDate.get(Calendar.YEAR) &&
                                    now.get(Calendar.DAY_OF_YEAR) == lastDate.get(Calendar.DAY_OF_YEAR)
                
                if (isConsecutive) {
                    streak += 1
                    if (streak > maxStreak) maxStreak = streak
                } else {
                    streak = 1 // Reset streak
                }
            }
        }
        
        repository.updateUserProfile(profile.copy(
            streakDays = streak,
            maxStreakDays = maxStreak,
            lastStudyDate = System.currentTimeMillis()
        ))
        
        updateProgress("streak_3", streak, absoluteProgress = true)
        updateProgress("streak_7", streak, absoluteProgress = true)
        updateProgress("streak_30", streak, absoluteProgress = true)
    }

    suspend fun updateProgress(achievementId: String, amount: Int = 1, absoluteProgress: Boolean = false) {
        val profile = repository.getUserProfile().firstOrNull() ?: return
        if (!profile.gamificationEnabled) return

        val achievements = repository.getAllAchievements().firstOrNull() ?: emptyList()
        val achievement = achievements.find { it.id == achievementId } ?: return
        
        if (achievement.isUnlocked) return
        
        val newProgress = if (absoluteProgress) {
            amount
        } else {
            achievement.progress + amount
        }
        
        if (newProgress >= achievement.maxProgress) {
            repository.updateAchievement(achievement.copy(
                progress = achievement.maxProgress,
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis()
            ))
            addXp(50)
        } else {
            repository.updateAchievement(achievement.copy(progress = newProgress))
        }
    }

    suspend fun completeAction(actionType: String) {
        val profile = repository.getUserProfile().firstOrNull() ?: return
        if (!profile.gamificationEnabled) return

        updateStreak()

        when (actionType) {
            "TASK_COMPLETED" -> {
                addXp(10)
                updateProgress("tasks_100", 1)
                updateChallengeProgress("TASK", 1)
            }
            "POMODORO_COMPLETED" -> {
                addXp(20)
                updateProgress("first_pomodoro", 1)
                updateChallengeProgress("POMODORO", 1)
            }
            "SESSION_COMPLETED" -> {
                addXp(15)
                updateProgress("hours_100", 1)
            }
            "REVISION_COMPLETED" -> {
                addXp(15)
                updateProgress("flashcards_100", 1)
                updateChallengeProgress("REVISION", 1)
            }
            "SIMULATION_COMPLETED" -> {
                addXp(50)
                updateProgress("first_sim", 1)
            }
            "ESSAY_COMPLETED" -> {
                addXp(30)
                updateProgress("first_essay", 1)
                updateChallengeProgress("ESSAY", 1)
            }
        }
    }

    private suspend fun updateChallengeProgress(type: String, amount: Int) {
        val challenges = repository.getAllChallenges().firstOrNull() ?: emptyList()
        val now = System.currentTimeMillis()
        
        challenges.forEach { challenge ->
            if (!challenge.isCompleted && challenge.expiresAt > now) {
                var match = false
                if (type == "TASK" && challenge.id.contains("task")) match = true
                if (type == "POMODORO" && challenge.id.contains("pomodoro")) match = true
                if (type == "REVISION" && challenge.id.contains("revision")) match = true
                if (type == "ESSAY" && challenge.id.contains("essay")) match = true
                if (type == "SESSION" && challenge.id.contains("hours")) match = true

                if (match) {
                    val newProgress = challenge.currentAmount + amount
                    if (newProgress >= challenge.requiredAmount) {
                        repository.updateChallenge(challenge.copy(
                            currentAmount = challenge.requiredAmount,
                            isCompleted = true
                        ))
                        addXp(challenge.xpReward)
                    } else {
                        repository.updateChallenge(challenge.copy(currentAmount = newProgress))
                    }
                }
            }
        }
    }
}
