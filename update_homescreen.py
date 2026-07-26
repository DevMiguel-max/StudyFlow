import re

with open("app/src/main/java/com/example/presentation/screens/HomeScreen.kt", "r") as f:
    content = f.read()

content = content.replace("AppTopBar(level = 12, streak = 14, xp = 1200)", "AppTopBar(level = state.userProfile?.level ?: 1, streak = state.userProfile?.streakDays ?: 0, xp = state.userProfile?.xp ?: 0)")

with open("app/src/main/java/com/example/presentation/screens/HomeScreen.kt", "w") as f:
    f.write(content)
