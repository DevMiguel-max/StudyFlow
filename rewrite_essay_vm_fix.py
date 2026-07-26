with open("app/src/main/java/com/example/presentation/viewmodel/EssayViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("val isLoading: Boolean = false)", "val isLoading: Boolean = false, val error: String? = null)")
content = content.replace("class EssayViewModel(private val repository: StudyFlowRepository, private val gamificationManager: com.example.domain.manager.GamificationManager) : ViewModel()", "class EssayViewModel(private val repository: StudyFlowRepository, private val gamificationManager: com.example.domain.manager.GamificationManager, private val aiService: com.example.domain.ai.AIEssayCorrectorService = com.example.domain.ai.AIEssayCorrectorService()) : ViewModel()")
content = content.replace("val json = Json { ignoreUnknownKeys = true }", "val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }")

with open("app/src/main/java/com/example/presentation/viewmodel/EssayViewModel.kt", "w") as f:
    f.write(content)
