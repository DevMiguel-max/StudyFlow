import re

with open("app/src/main/java/com/example/presentation/viewmodel/StatisticsViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("    val insights: List<String> = emptyList()", "    val insights: List<String> = emptyList(),\n    val userProfile: com.example.data.local.UserProfile? = null")

with open("app/src/main/java/com/example/presentation/viewmodel/StatisticsViewModel.kt", "w") as f:
    f.write(content)
