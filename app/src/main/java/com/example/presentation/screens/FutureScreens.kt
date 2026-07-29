package com.example.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import com.example.presentation.components.AppTopBar
import com.example.presentation.components.EmptyState
import com.example.presentation.viewmodel.PreparationViewModel
import com.example.presentation.viewmodel.SimulationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    navController: NavController,
    examType: String,
    prepViewModel: PreparationViewModel = koinViewModel(),
    simViewModel: SimulationViewModel = koinViewModel()
) {
    if (examType == "ENEM" || examType == "Vestibulares" || examType == "Concursos" || examType == "Faculdade") {
        val prepState by prepViewModel.state.collectAsState()
        val simState by simViewModel.state.collectAsState()
        
        val typeFilter = when (examType) {
            "ENEM" -> "ENEM"
            "Vestibulares" -> "Vestibular"
            "Concursos" -> "Concurso"
            "Faculdade" -> "Faculdade"
            else -> examType
        }
        
        val goals = prepState.allGoals.filter { it.type == typeFilter }
        val simulations = simState.simulations.filter { it.examType == typeFilter }

        Scaffold(
            topBar = {
                AppTopBar(title = examType)
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                item {
                    Text(
                        text = "Metas de Estudo",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (goals.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhuma meta definida para $examType ainda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    items(goals) { goal ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = goal.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                goal.date?.let {
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                                    Text(text = "Data: $dateStr", style = MaterialTheme.typography.bodyMedium)
                                }
                                goal.targetScore?.let {
                                    Text(text = "Nota Alvo: $it", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                item {
                    Text(
                        text = "Simulados Recentes",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                if (simulations.isEmpty()) {
                    item {
                        Text(
                            text = "Nenhum simulado registrado para $examType.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(simulations) { sim ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = sim.name, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(sim.date))
                                    Text(text = dateStr, style = MaterialTheme.typography.bodySmall)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${sim.score.toInt()} pts",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${sim.correctAnswers}/${sim.totalQuestions} acertos",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                AppTopBar(title = examType)
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                EmptyState(
                    icon = "🚧",
                    title = "Estatísticas específicas chegam em breve",
                    description = "Use a seção de Simulados para registrar seu desempenho por enquanto."
                )
            }
        }
    }
}
