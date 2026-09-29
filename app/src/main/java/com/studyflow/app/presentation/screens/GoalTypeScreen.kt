package com.studyflow.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyflow.app.domain.repository.StudyFlowRepository
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalTypeScreen(
    navController: NavController,
    targetType: String,
    repository: StudyFlowRepository = koinInject()
) {
    val goals by repository.getAllStudyGoals().collectAsState(initial = emptyList())
    val simulations by repository.getAllSimulations().collectAsState(initial = emptyList())
    
    val filteredGoals = goals.filter { it.type == targetType }
    val filteredSimulations = simulations.filter { it.examType == targetType }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(targetType, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (filteredGoals.isEmpty()) {
                FloatingActionButton(onClick = { navController.navigate("exam_goal_form") }) {
                    Icon(Icons.Default.Add, contentDescription = "Criar Objetivo")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (filteredGoals.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum objetivo do tipo $targetType encontrado.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.navigate("exam_goal_form") }) {
                        Text("Criar Objetivo")
                    }
                }
            } else {
                item {
                    Text("Seus Objetivos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                items(filteredGoals) { goal ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (!goal.institution.isNullOrBlank()) {
                                Text("Instituição: ${goal.institution}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (!goal.course.isNullOrBlank()) {
                                Text("Curso: ${goal.course}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text("Status: ${goal.status}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Simulados Relacionados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                
                if (filteredSimulations.isEmpty()) {
                    item {
                        Text("Nenhum simulado registrado para $targetType.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    items(filteredSimulations) { sim ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(sim.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Nota: ${sim.score}", style = MaterialTheme.typography.bodyMedium)
                                Text("Acertos: ${sim.correctAnswers}/${sim.totalQuestions}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
