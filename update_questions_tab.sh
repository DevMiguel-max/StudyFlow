sed -i '/fun QuestionsTab(/c \
@Composable\
fun QuestionsTab(\
    quantity: Int, onQuantityChange: (Int) -> Unit,\
    difficulty: String, onDifficultyChange: (String) -> Unit,\
    questionType: String, onQuestionTypeChange: (String) -> Unit,\
    subjects: List<Subject>, selectedSubjectId: Int?, onSubjectChange: (Int) -> Unit,\
    onGenerate: () -> Unit,\
    generatedQuestions: List<GeneratedQuestion>,\
    onAnswer: (GeneratedQuestion, String) -> Unit\
) {' app/src/main/java/com/example/presentation/screens/MaterialGeneratorScreen.kt
