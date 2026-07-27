import re

# Update StudySessionViewModel
with open("app/src/main/java/com/example/presentation/viewmodel/StudySessionViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "class StudySessionViewModel(private val repository: StudyFlowRepository) : ViewModel()",
    "class StudySessionViewModel(private val repository: StudyFlowRepository, private val gamificationManager: com.example.domain.manager.GamificationManager) : ViewModel()"
)
# Inside saveSession, after insertStudySession
content = content.replace(
    "repository.insertStudySession(session)",
    "repository.insertStudySession(session)\n            gamificationManager.completeAction(\"SESSION_COMPLETED\")\n            if (session.type == \"Pomodoro\") gamificationManager.completeAction(\"POMODORO_COMPLETED\")"
)
with open("app/src/main/java/com/example/presentation/viewmodel/StudySessionViewModel.kt", "w") as f:
    f.write(content)

# Update AppModule.kt
with open("app/src/main/java/com/example/di/AppModule.kt", "r") as f:
    content = f.read()

content = content.replace("viewModel { TaskViewModel(get()) }", "viewModel { TaskViewModel(get(), get()) }")
content = content.replace("viewModel { SimulationViewModel(get()) }", "viewModel { SimulationViewModel(get(), get()) }")
content = content.replace("viewModel { EssayViewModel(get()) }", "viewModel { EssayViewModel(get(), get()) }")
content = content.replace("viewModel { StudySessionViewModel(get()) }", "viewModel { StudySessionViewModel(get(), get()) }")

with open("app/src/main/java/com/example/di/AppModule.kt", "w") as f:
    f.write(content)
