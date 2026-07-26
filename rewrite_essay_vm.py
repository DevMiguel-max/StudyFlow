with open("app/src/main/java/com/example/presentation/viewmodel/EssayViewModel.kt", "r") as f:
    content = f.read()

# Update requestCorrection using string slicing instead of regex
new_request = """
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun requestCorrection(submissionId: Int, model: String = "gemini-1.5-pro-latest", rigor: String = "Normal", detailed: Boolean = true) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val submission = repository.getEssaySubmissionById(submissionId) ?: throw Exception("Redação não encontrada.")
                if (submission.text.trim().split("\\s+".toRegex()).size < 50) {
                    throw Exception("O texto é muito curto para ser corrigido. Escreva pelo menos 50 palavras.")
                }
                
                val theme = state.value.themes.find { it.id == submission.themeId } ?: repository.getAllEssayThemes().first().find { it.id == submission.themeId }
                val themeTitle = theme?.title ?: "Tema livre"
                
                val texts = repository.getMotivationalTextsForTheme(submission.themeId)
                val motivationalTexts = texts.joinToString("\\n\\n") { "${it.title}: ${it.content}" }
                
                val result = aiService.correctEssay(
                    modelName = model,
                    temperature = if (rigor == "Alto") 0.2f else 0.7f,
                    essayText = submission.text,
                    themeTitle = themeTitle,
                    motivationalTexts = motivationalTexts
                )
                
                val json = Json { ignoreUnknownKeys = true }
                
                val compDetailsMap = mapOf(
                    "comp1" to result.comp1,
                    "comp2" to result.comp2,
                    "comp3" to result.comp3,
                    "comp4" to result.comp4,
                    "comp5" to result.comp5
                )
                
                val correction = EssayCorrection(
                    submissionId = submissionId,
                    comp1 = result.comp1.score,
                    comp2 = result.comp2.score,
                    comp3 = result.comp3.score,
                    comp4 = result.comp4.score,
                    comp5 = result.comp5.score,
                    totalScore = result.totalScore,
                    strengths = "Ver detalhes nas competências.",
                    weaknesses = "Ver detalhes nas competências.",
                    suggestions = "Ver detalhes nas competências.",
                    date = System.currentTimeMillis(),
                    generalComment = result.generalComment,
                    essayLevel = result.essayLevel,
                    performanceEstimate = result.performanceEstimate,
                    revisedVersion = result.revisedVersion,
                    detailedAnalysisJson = json.encodeToString(result.detailedErrors),
                    competenciesDetailsJson = json.encodeToString(compDetailsMap),
                    modelUsed = model,
                    isDetailed = detailed
                )
                
                repository.insertEssayCorrection(correction)
                repository.updateEssaySubmission(submission.copy(status = "graded"))
                
                loadSubmission(submissionId)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Ocorreu um erro ao corrigir a redação.") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
"""

start_idx = content.find("fun requestCorrection(submissionId: Int)")
if start_idx != -1:
    content = content[:start_idx] + new_request

with open("app/src/main/java/com/example/presentation/viewmodel/EssayViewModel.kt", "w") as f:
    f.write(content)
