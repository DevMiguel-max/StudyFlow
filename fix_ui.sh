sed -i '275,$d' app/src/main/java/com/example/presentation/screens/MaterialGeneratorScreen.kt

cat << 'INNER_EOF' >> app/src/main/java/com/example/presentation/screens/MaterialGeneratorScreen.kt
@Composable
fun QuestionsTab(
    quantity: Int, onQuantityChange: (Int) -> Unit,
    difficulty: String, onDifficultyChange: (String) -> Unit,
    questionType: String, onQuestionTypeChange: (String) -> Unit,
    subjects: List<Subject>, selectedSubjectId: Int?, onSubjectChange: (Int) -> Unit,
    onGenerate: () -> Unit,
    generatedQuestions: List<com.example.data.local.GeneratedQuestion>,
    onAnswer: (com.example.data.local.GeneratedQuestion, String) -> Unit
) {
    Text("Configurações das Questões", style = MaterialTheme.typography.titleMedium)
    
    Text("Quantidade: $quantity")
    Slider(value = quantity.toFloat(), onValueChange = { onQuantityChange(it.toInt()) }, valueRange = 1f..15f)
    
    Text("Tipo")
    val types = listOf("MÚLTIPLA ESCOLHA", "VERDADEIRO/FALSO", "DISSERTATIVA")
    types.forEach { type ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onQuestionTypeChange(type) }) {
            RadioButton(selected = questionType == type, onClick = { onQuestionTypeChange(type) })
            Text(type, modifier = Modifier.padding(start = 8.dp))
        }
    }
    
    Text("Dificuldade")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("FÁCIL", "MÉDIO", "DIFÍCIL", "ADAPTATIVO").forEach { diff ->
            FilterChip(selected = difficulty == diff, onClick = { onDifficultyChange(diff) }, label = { Text(diff) })
        }
    }
    
    Text("Matéria de Destino")
    if (subjects.isEmpty()) {
        Text("Crie uma matéria primeiro.", color = MaterialTheme.colorScheme.error)
    } else {
        subjects.forEach { subject ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onSubjectChange(subject.id) }) {
                RadioButton(selected = selectedSubjectId == subject.id, onClick = { onSubjectChange(subject.id) })
                Text(subject.name, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
    
    Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth(), enabled = selectedSubjectId != null) {
        Text("Gerar Questões")
    }
    
    if (generatedQuestions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("${generatedQuestions.size} questões geradas com sucesso!", color = MaterialTheme.colorScheme.primary)
        generatedQuestions.forEach { q ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(q.statement, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    val optionsList = try {
                        kotlinx.serialization.json.Json.decodeFromString<List<String>>(q.options)
                    } catch(e: Exception) { emptyList() }
                    optionsList.forEach { opt ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { if (!q.isAnswered) onAnswer(q, opt) }) {
                            RadioButton(selected = false, onClick = { if (!q.isAnswered) onAnswer(q, opt) }, enabled = !q.isAnswered)
                            Text(opt, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    if (optionsList.isEmpty() && !q.isAnswered) {
                        Button(onClick = { onAnswer(q, "Resposta dissertativa enviada") }) {
                            Text("Ver Resposta e Explicação")
                        }
                    }
                    if (q.isAnswered) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(if (q.wasCorrect) "✅ Resposta Correta!" else "❌ Resposta Incorreta", color = if (q.wasCorrect) androidx.compose.ui.graphics.Color.Green else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Resposta Correta: ${q.correctAnswer}", color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        dev.jeziellago.compose.markdowntext.MarkdownText(markdown = "**Explicação:**\n${q.explanation}")
                    }
                }
            }
        }
    }
}

@Composable
fun MindMapTab(
    onGenerate: () -> Unit, generatedMindMap: String?
) {
    Text("Geração de Mapa Mental", style = MaterialTheme.typography.titleMedium)
    Text("A IA irá estruturar o conteúdo de forma hierárquica, facilitando a criação de mapas mentais.")
    
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth()) {
        Text("Gerar Estrutura")
    }
    
    if (generatedMindMap != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Estrutura Gerada", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            dev.jeziellago.compose.markdowntext.MarkdownText(markdown = generatedMindMap, modifier = Modifier.padding(16.dp))
        }
    }
}
INNER_EOF
