import re

with open("app/src/main/java/com/example/presentation/viewmodel/ProfileViewModel.kt", "r") as f:
    content = f.read()

update_method = """
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
"""

content = content.replace("fun updateSettings(gamification: Boolean, sounds: Boolean, notifications: Boolean)", update_method.strip() + "\n\n    fun updateSettings(gamification: Boolean, sounds: Boolean, notifications: Boolean)")

with open("app/src/main/java/com/example/presentation/viewmodel/ProfileViewModel.kt", "w") as f:
    f.write(content)
