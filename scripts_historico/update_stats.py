import re

# Update StatisticsViewModel
with open("app/src/main/java/com/example/presentation/viewmodel/StatisticsViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val recommendedMethods: List<StudyMethodModel> = emptyList()",
    "val recommendedMethods: List<StudyMethodModel> = emptyList(),\n    val userProfile: com.example.data.local.UserProfile? = null"
)

content = content.replace(
    "repository.getAllStudySessions()",
    "repository.getAllStudySessions(),\n        repository.getUserProfile()"
)

content = content.replace(
    ") { tasks, sessions ->",
    ") { tasks, sessions, profile ->"
)

content = content.replace(
    "recommendedMethods = recommendedMethods",
    "recommendedMethods = recommendedMethods,\n            userProfile = profile"
)

with open("app/src/main/java/com/example/presentation/viewmodel/StatisticsViewModel.kt", "w") as f:
    f.write(content)

# Update StatisticsScreen
with open("app/src/main/java/com/example/presentation/screens/StatisticsScreen.kt", "r") as f:
    screen_content = f.read()

screen_content = screen_content.replace(
    "AppTopBar(level = 15, streak = state.studyStreak, xp = 2400)",
    "AppTopBar(level = state.userProfile?.level ?: 1, streak = state.userProfile?.streakDays ?: 0, xp = state.userProfile?.xp ?: 0)"
)

with open("app/src/main/java/com/example/presentation/screens/StatisticsScreen.kt", "w") as f:
    f.write(screen_content)
