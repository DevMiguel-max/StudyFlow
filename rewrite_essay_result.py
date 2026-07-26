with open("app/src/main/java/com/example/presentation/screens/EssayResultScreen.kt", "r") as f:
    content = f.read()

new_content = """package com.example.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.presentation.components.AppCard
import com.example.presentation.viewmodel.EssayViewModel
import org.koin.androidx.compose.koinViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import com.example.domain.ai.DetailedError
import com.example.domain.ai.CompetencyDetail
import dev.jeziellago.compose.markdowntext.MarkdownText
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AutoFixHigh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EssayResultScreen(
    navController: NavController,
    submissionId: Int,
    viewModel: EssayViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(submissionId) {
        viewModel.loadSubmission(submissionId)
    }
    
    val correction = state.currentCorrection
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Correção Inteligente", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("A IA está analisando sua redação...")
                }
            }
        } else if (state.error != null) {
             Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(state.error!!, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) { Text("Voltar") }
                }
            }
        } else if (correction == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val json = Json { ignoreUnknownKeys = true }
            var detailedErrors: List<DetailedError> = emptyList()
            var compDetails: Map<String, CompetencyDetail> = emptyMap()
            
            try {
                if (correction.detailedAnalysisJson.isNotBlank()) {
                    detailedErrors = json.decodeFromString(correction.detailedAnalysisJson)
                }
                if (correction.competenciesDetailsJson.isNotBlank()) {
                    compDetails = json.decodeFromString(correction.competenciesDetailsJson)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("Nota Final (ENEM)", style = MaterialTheme.typography.titleMedium)
                        Text("${correction.totalScore}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        if (correction.essayLevel.isNotBlank()) {
                            SuggestionChip(onClick = {}, label = { Text(correction.essayLevel) })
                        }
                    }
                }
                
                if (correction.generalComment.isNotBlank()) {
                    AppCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Comentário Geral", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(correction.generalComment)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Estimativa: ${correction.performanceEstimate}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Text("Competências ENEM", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                if (compDetails.isNotEmpty()) {
                    compDetails["comp1"]?.let { CompetenceDetailedRow("C1: Norma Culta", it) }
                    compDetails["comp2"]?.let { CompetenceDetailedRow("C2: Compreensão e Tema", it) }
                    compDetails["comp3"]?.let { CompetenceDetailedRow("C3: Organização de Ideias", it) }
                    compDetails["comp4"]?.let { CompetenceDetailedRow("C4: Coesão", it) }
                    compDetails["comp5"]?.let { CompetenceDetailedRow("C5: Proposta de Intervenção", it) }
                } else {
                    CompetenceRow("C1: Norma Culta", correction.comp1)
                    CompetenceRow("C2: Compreensão e Tema", correction.comp2)
                    CompetenceRow("C3: Organização de Ideias", correction.comp3)
                    CompetenceRow("C4: Coesão", correction.comp4)
                    CompetenceRow("C5: Proposta de Intervenção", correction.comp5)
                }
                
                if (detailedErrors.isNotEmpty()) {
                    Text("Análise Detalhada de Desvios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                    detailedErrors.forEach { error ->
                        AppCard(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(error.type, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("\\"${error.snippet}\\"", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(error.explanation, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Sugestão: ${error.suggestion}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                if (correction.revisedVersion.isNotBlank()) {
                    Text("Versão Revisada (IA)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                    AppCard(containerColor = MaterialTheme.colorScheme.surface) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            MarkdownText(markdown = correction.revisedVersion)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CompetenceRow(title: String, score: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text("$score / 200", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
    LinearProgressIndicator(
        progress = score / 200f,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun CompetenceDetailedRow(title: String, detail: CompetencyDetail) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    AppCard(
        modifier = Modifier.fillMaxWidth().androidx.compose.foundation.clickable { expanded = !expanded },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("${detail.score} / 200", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(
                progress = detail.score / 200f,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
            )
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(detail.explanation, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("✅ ${detail.strengths}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF065F46))
                Spacer(modifier = Modifier.height(4.dp))
                Text("⚠️ ${detail.weaknesses}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(4.dp))
                Text("💡 ${detail.suggestions}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF92400E))
            } else {
                Text("Toque para ver detalhes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
"""

with open("app/src/main/java/com/example/presentation/screens/EssayResultScreen.kt", "w") as f:
    f.write(new_content)
