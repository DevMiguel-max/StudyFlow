import re

with open("app/src/main/java/com/example/presentation/navigation/AppNavigation.kt", "r") as f:
    content = f.read()

content = content.replace('composable("home") { HomeScreen(navController) }', 'composable("home") { HomeScreen(navController) }\n        composable("ai_tutor") { com.example.presentation.screens.AITutorScreen(navController) }')

with open("app/src/main/java/com/example/presentation/navigation/AppNavigation.kt", "w") as f:
    f.write(content)
