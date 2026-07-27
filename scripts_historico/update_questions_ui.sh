sed -i '/if (generatedQuestions.isNotEmpty()) {/,/^}/c \
    if (generatedQuestions.isNotEmpty()) {\
        Spacer(modifier = Modifier.height(16.dp))\
        Text("${generatedQuestions.size} questões geradas com sucesso!", color = MaterialTheme.colorScheme.primary)\
        generatedQuestions.forEach { q ->\
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {\
                Column(modifier = Modifier.padding(16.dp)) {\
                    Text(q.statement, style = MaterialTheme.typography.bodyLarge)\
                    Spacer(modifier = Modifier.height(8.dp))\
                    val optionsList = try {\
                        kotlinx.serialization.json.Json.decodeFromString<List<String>>(q.options)\
                    } catch(e: Exception) { emptyList() }\
                    optionsList.forEach { opt ->\
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { if (!q.isAnswered) onAnswer(q, opt) }) {\
                            RadioButton(selected = false, onClick = { if (!q.isAnswered) onAnswer(q, opt) }, enabled = !q.isAnswered)\
                            Text(opt, modifier = Modifier.padding(start = 8.dp))\
                        }\
                    }\
                    if (optionsList.isEmpty() && !q.isAnswered) {\
                        Button(onClick = { onAnswer(q, "Resposta dissertativa enviada") }) {\
                            Text("Ver Resposta e Explicação")\
                        }\
                    }\
                    if (q.isAnswered) {\
                        Spacer(modifier = Modifier.height(8.dp))\
                        Text(if (q.wasCorrect) "✅ Resposta Correta!" else "❌ Resposta Incorreta", color = if (q.wasCorrect) androidx.compose.ui.graphics.Color.Green else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)\
                        Spacer(modifier = Modifier.height(4.dp))\
                        Text("Resposta Correta: ${q.correctAnswer}", color = MaterialTheme.colorScheme.primary)\
                        Spacer(modifier = Modifier.height(4.dp))\
                        MarkdownText(markdown = "**Explicação:**\n${q.explanation}")\
                    }\
                }\
            }\
        }\
    }' app/src/main/java/com/example/presentation/screens/MaterialGeneratorScreen.kt
