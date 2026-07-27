sed -i '/fun clearState()/i \
    fun getAdaptiveDifficulty(subjectName: String): String {\
        val subjectQuestions = savedQuestions.value.filter { it.subject == subjectName && it.isAnswered }\
        if (subjectQuestions.size < 3) return "MÉDIO"\
        val recent = subjectQuestions.sortedByDescending { it.createdAt }.take(5)\
        val correctCount = recent.count { it.wasCorrect }\
        val rate = correctCount.toFloat() / recent.size\
        return when {\
            rate >= 0.8f -> "DIFÍCIL"\
            rate <= 0.4f -> "FÁCIL"\
            else -> "MÉDIO"\
        }\
    }\
' app/src/main/java/com/example/presentation/viewmodel/MaterialGeneratorViewModel.kt
