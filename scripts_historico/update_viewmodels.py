import re

# Update TaskViewModel
with open("app/src/main/java/com/example/presentation/viewmodel/TaskViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "class TaskViewModel(private val repository: StudyFlowRepository) : ViewModel()",
    "class TaskViewModel(private val repository: StudyFlowRepository, private val gamificationManager: com.example.domain.manager.GamificationManager) : ViewModel()"
)
content = content.replace(
    "repository.updateTask(updated)",
    "repository.updateTask(updated)\n            if (updated.status == \"completed\") {\n                gamificationManager.completeAction(\"TASK_COMPLETED\")\n            }"
)
with open("app/src/main/java/com/example/presentation/viewmodel/TaskViewModel.kt", "w") as f:
    f.write(content)

# Update SimulationViewModel
with open("app/src/main/java/com/example/presentation/viewmodel/SimulationViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "class SimulationViewModel(private val repository: StudyFlowRepository) : ViewModel()",
    "class SimulationViewModel(private val repository: StudyFlowRepository, private val gamificationManager: com.example.domain.manager.GamificationManager) : ViewModel()"
)
content = content.replace(
    "repository.insertSimulation(simulation)",
    "repository.insertSimulation(simulation)\n            gamificationManager.completeAction(\"SIMULATION_COMPLETED\")"
)
with open("app/src/main/java/com/example/presentation/viewmodel/SimulationViewModel.kt", "w") as f:
    f.write(content)

# Update EssayViewModel
with open("app/src/main/java/com/example/presentation/viewmodel/EssayViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "class EssayViewModel(private val repository: StudyFlowRepository) : ViewModel()",
    "class EssayViewModel(private val repository: StudyFlowRepository, private val gamificationManager: com.example.domain.manager.GamificationManager) : ViewModel()"
)
content = content.replace(
    "val id = repository.insertEssaySubmission(newSub)",
    "val id = repository.insertEssaySubmission(newSub)\n                gamificationManager.completeAction(\"ESSAY_COMPLETED\")"
)
with open("app/src/main/java/com/example/presentation/viewmodel/EssayViewModel.kt", "w") as f:
    f.write(content)
