package com.example.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.local.AnalyzedDocument
import com.example.data.local.Subject
import com.example.presentation.viewmodel.MaterialGeneratorViewModel
import org.koin.androidx.compose.koinViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialGeneratorScreen(
    navController: NavController,
    viewModel: MaterialGeneratorViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val documents by viewModel.analyzedDocuments.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    
    var sourceText by remember { mutableStateOf("") }
    var selectedDoc by remember { mutableStateOf<AnalyzedDocument?>(null) }
    var isDocDropdownExpanded by remember { mutableStateOf(false) }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Resumo", "Flashcards", "Questões", "Mapa Mental")
    
    // Configs
    var summaryType by remember { mutableStateOf("SUMMARY_QUICK") }
    var difficulty by remember { mutableStateOf("MÉDIO") }
    var quantity by remember { mutableStateOf(5) }
    var questionType by remember { mutableStateOf("MÚLTIPLA ESCOLHA") }
    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerador Inteligente") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fonte
                Text("Fonte de Conteúdo", style = MaterialTheme.typography.titleMedium)
                
                ExposedDropdownMenuBox(
                    expanded = isDocDropdownExpanded,
                    onExpandedChange = { isDocDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedDoc?.title ?: "Texto manual",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selecione um Documento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDocDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isDocDropdownExpanded,
                        onDismissRequest = { isDocDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Texto manual (digite abaixo)") },
                            onClick = { 
                                selectedDoc = null
                                sourceText = ""
                                isDocDropdownExpanded = false 
                            }
                        )
                        documents.forEach { doc ->
                            DropdownMenuItem(
                                text = { Text(doc.title) },
                                onClick = { 
                                    selectedDoc = doc
                                    sourceText = doc.textContent
                                    isDocDropdownExpanded = false 
                                }
                            )
                        }
                    }
                }
                
                if (selectedDoc == null) {
                    OutlinedTextField(
                        value = sourceText,
                        onValueChange = { sourceText = it },
                        label = { Text("Cole o conteúdo aqui") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 10
                    )
                }

                Divider()

                if (state.isGenerating) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.generationMessage)
                    }
                } else if (state.error != null) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.clearState() }) { Text("Tentar Novamente") }
                } else {
                    when (selectedTab) {
                        0 -> SummaryTab(
                            type = summaryType,
                            onTypeChange = { summaryType = it },
                            onGenerate = { viewModel.generateSummary(sourceText, summaryType, "Resumo Gerado") },
                            generatedSummary = state.generatedSummary
                        )
                        1 -> FlashcardTab(
                            quantity = quantity,
                            onQuantityChange = { quantity = it },
                            difficulty = difficulty,
                            onDifficultyChange = { difficulty = it },
                            subjects = subjects,
                            selectedSubjectId = selectedSubjectId,
                            onSubjectChange = { selectedSubjectId = it },
                            onGenerate = { 
                                if (selectedSubjectId != null) {
                                    viewModel.generateFlashcards(sourceText, quantity, difficulty, selectedSubjectId!!) 
                                }
                            },
                            generatedCount = state.generatedFlashcards.size
                        )
                        2 -> QuestionsTab(
                            quantity = quantity,
                            onQuantityChange = { quantity = it },
                            difficulty = difficulty,
                            onDifficultyChange = { difficulty = it },
                            questionType = questionType,
                            onQuestionTypeChange = { questionType = it },
                            subjects = subjects,
                            selectedSubjectId = selectedSubjectId,
                            onSubjectChange = { selectedSubjectId = it },
                            onGenerate = {
                                if (selectedSubjectId != null) {
                                    viewModel.generateQuestions(sourceText, quantity, difficulty, questionType, selectedSubjectId!!)
                                }
                            },
                            generatedQuestions = state.generatedQuestions,
                            onAnswer = { q, a -> viewModel.answerQuestion(q, a) }
                        )
                        3 -> MindMapTab(
                            onGenerate = { viewModel.generateSummary(sourceText, "MIND_MAP", "Mapa Mental Gerado") },
                            generatedMindMap = state.generatedSummary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryTab(
    type: String, onTypeChange: (String) -> Unit, onGenerate: () -> Unit, generatedSummary: String?
) {
    val options = mapOf(
        "SUMMARY_QUICK" to "Rápido",
        "SUMMARY_COMPLETE" to "Completo",
        "SUMMARY_TOPIC" to "Por Tópicos",
        "SUMMARY_CHRONO" to "Cronológico",
        "SUMMARY_REVIEW" to "Revisão",
        "SUMMARY_CONTEST" to "Focado em Concurso",
        "SUMMARY_ENEM" to "Focado no ENEM"
    )
    
    Text("Configurações do Resumo", style = MaterialTheme.typography.titleMedium)
    options.forEach { (key, label) ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onTypeChange(key) }) {
            RadioButton(selected = type == key, onClick = { onTypeChange(key) })
            Text(label, modifier = Modifier.padding(start = 8.dp))
        }
    }
    
    Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth()) {
        Text("Gerar Resumo")
    }
    
    if (generatedSummary != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Resultado", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            MarkdownText(markdown = generatedSummary, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun FlashcardTab(
    quantity: Int, onQuantityChange: (Int) -> Unit,
    difficulty: String, onDifficultyChange: (String) -> Unit,
    subjects: List<Subject>, selectedSubjectId: Int?, onSubjectChange: (Int) -> Unit,
    onGenerate: () -> Unit,
    generatedCount: Int
) {
    Text("Configurações dos Flashcards", style = MaterialTheme.typography.titleMedium)
    
    Text("Quantidade: $quantity")
    Slider(value = quantity.toFloat(), onValueChange = { onQuantityChange(it.toInt()) }, valueRange = 1f..20f)
    
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
        Text("Gerar Flashcards")
    }
    
    if (generatedCount > 0) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("$generatedCount flashcards gerados e salvos com sucesso na caixa de revisão 1!", color = MaterialTheme.colorScheme.primary)
    }
}

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
