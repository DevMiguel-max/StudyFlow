with open("app/src/main/java/com/example/presentation/viewmodel/EssayViewModel.kt", "r") as f:
    content = f.read()

new_submit = """
    fun submitForCorrection(text: String, timeSpentSeconds: Int) {
        val themeId = state.value.currentTheme?.id ?: return
        val currentSub = state.value.currentSubmission
        
        viewModelScope.launch {
            val wordCount = text.split("\\s+".toRegex()).count { it.isNotBlank() }
            val submissionId: Int
            
            if (currentSub != null) {
                val updated = currentSub.copy(text = text, timeSpentSeconds = timeSpentSeconds, status = "sent", wordCount = wordCount, charCount = text.length)
                repository.updateEssaySubmission(updated)
                _state.update { it.copy(currentSubmission = updated) }
                submissionId = updated.id
            } else {
                val newSub = EssaySubmission(
                    themeId = themeId,
                    text = text,
                    date = System.currentTimeMillis(),
                    timeSpentSeconds = timeSpentSeconds,
                    status = "sent",
                    wordCount = wordCount,
                    charCount = text.length
                )
                submissionId = repository.insertEssaySubmission(newSub).toInt()
                gamificationManager.completeAction("ESSAY_COMPLETED")
                _state.update { it.copy(currentSubmission = newSub.copy(id = submissionId)) }
            }
            
            requestCorrection(submissionId)
        }
    }
"""

content = content.replace("fun requestCorrection(submissionId: Int", new_submit + "\n    fun requestCorrection(submissionId: Int")

with open("app/src/main/java/com/example/presentation/viewmodel/EssayViewModel.kt", "w") as f:
    f.write(content)
