package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.Achievement
import com.example.data.local.Challenge
import com.example.data.local.UserProfile
import com.example.domain.manager.GamificationManager
import com.example.domain.repository.StudyFlowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileState(
    val userProfile: UserProfile? = null,
    val achievements: List<Achievement> = emptyList(),
    val challenges: List<Challenge> = emptyList(),
    val requiredXpNextLevel: Int = 100,
    val isLoading: Boolean = false
)

class ProfileViewModel(
    private val repository: StudyFlowRepository,
    private val gamificationManager: GamificationManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = combine(
        _state,
        repository.getUserProfile(),
        repository.getAllAchievements(),
        repository.getAllChallenges()
    ) { state, profile, achievements, challenges ->
        state.copy(
            userProfile = profile,
            achievements = achievements,
            challenges = challenges,
            requiredXpNextLevel = profile?.let { gamificationManager.getRequiredXpForLevel(it.level + 1) } ?: 100
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileState())

    init {
        viewModelScope.launch {
            // Setup initial profile and achievements if empty
            var profile = repository.getUserProfile().stateIn(viewModelScope).value
            if (profile == null) {
                profile = UserProfile()
                repository.insertUserProfile(profile)
            }
            
            var achs = repository.getAllAchievements().stateIn(viewModelScope).value
            if (achs.isEmpty()) {
                val initialAchievements = listOf(
                    Achievement("first_pomodoro", "Foco Inicial", "Complete o primeiro Pomodoro.", "🍅", "Bronze", "Pomodoro", 0, 1),
                    Achievement("streak_3", "Iniciante Focado", "Complete 3 sessões consecutivas.", "🥉", "Bronze", "Geral", 0, 3),
                    Achievement("streak_7", "Chama Acesa", "Complete 7 sessões consecutivas.", "🔥", "Prata", "Geral", 0, 7),
                    Achievement("streak_30", "Mestre do Hábito", "Complete 30 sessões consecutivas.", "🏆", "Ouro", "Geral", 0, 30),
                    Achievement("first_essay", "Escritor Iniciante", "Escreva a primeira redação.", "✍️", "Bronze", "Redação", 0, 1),
                    Achievement("first_sim", "Preparado", "Faça o primeiro simulado.", "📝", "Prata", "Simulado", 0, 1),
                    Achievement("tasks_100", "Produtivo", "Conclua 100 tarefas.", "✅", "Ouro", "Tarefas", 0, 100),
                    Achievement("hours_100", "Veterano", "Complete 100 horas de estudo.", "⏱️", "Diamante", "Geral", 0, 100),
                    Achievement("flashcards_100", "Revisor", "Revise 100 flashcards.", "🧠", "Prata", "Revisão", 0, 100)
                )
                repository.insertAchievements(initialAchievements)
            }
            
            // Generate some challenges if empty
            var challenges = repository.getAllChallenges().stateIn(viewModelScope).value
            if (challenges.isEmpty()) {
                generateDailyChallenges()
            }
        }
    }

    private suspend fun generateDailyChallenges() {
        // Just mock some basic challenges for demo
        val expiry = System.currentTimeMillis() + 86400000 // 24 hours
        val challenges = listOf(
            Challenge("task_daily", "Produtividade", "Conclua 3 tarefas.", "DAILY", 3, 0, 50, false, expiry),
            Challenge("pomodoro_daily", "Foco Total", "Complete 2 Pomodoros.", "DAILY", 2, 0, 60, false, expiry),
            Challenge("revision_daily", "Revisão", "Faça 1 sessão de revisão.", "DAILY", 1, 0, 40, false, expiry)
        )
        repository.insertChallenges(challenges)
    }

    fun updateProfileInfo(name: String, mainGoal: String, avatarIcon: String) {
        viewModelScope.launch {
            val profile = state.value.userProfile ?: return@launch
            repository.updateUserProfile(profile.copy(
                name = name,
                mainGoal = mainGoal,
                avatarIcon = avatarIcon
            ))
        }
    }

    fun updateSettings(gamification: Boolean, sounds: Boolean, notifications: Boolean) {
        viewModelScope.launch {
            val profile = state.value.userProfile ?: return@launch
            repository.updateUserProfile(profile.copy(
                gamificationEnabled = gamification,
                soundsEnabled = sounds,
                notificationsEnabled = notifications
            ))
        }
    }
}
