import re

with open("app/src/main/java/com/example/presentation/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Add a third row to the Explorar section
new_row = """
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModuleCard(title = "StudyFlow AI", icon = "🤖", color = Color(0xFFFEE2E2), modifier = Modifier.weight(1f)) { navController.navigate("ai_tutor") }
                    ModuleCard(title = "Estatísticas", icon = "📈", color = Color(0xFFE0E7FF), modifier = Modifier.weight(1f)) { navController.navigate("statistics") }
                }"""

# Find the end of the row with study methods and tasks
target = """Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModuleCard(title = "Métodos", icon = "🧠", color = Color(0xFFFEF3C7), modifier = Modifier.weight(1f)) { navController.navigate("study_methods") }
                    ModuleCard(title = "Sessões", icon = "🏆", color = Color(0xFFE0E7FF), modifier = Modifier.weight(1f)) { navController.navigate("tasks") }
                }"""

content = content.replace(target, target + new_row)

with open("app/src/main/java/com/example/presentation/screens/HomeScreen.kt", "w") as f:
    f.write(content)
