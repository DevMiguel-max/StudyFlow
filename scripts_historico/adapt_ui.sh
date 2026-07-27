sed -i '/Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {/a \
        Button(onClick = { \
            val subj = subjects.find { it.id == selectedSubjectId }?.name ?: ""\
            // We need a reference to viewmodel.getAdaptiveDifficulty... \
        }) { Text("Adaptativo") }' app/src/main/java/com/example/presentation/screens/MaterialGeneratorScreen.kt
