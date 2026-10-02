package com.studyflow.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyflow.app.presentation.components.AppCard
import com.studyflow.app.presentation.components.RechartsVisualDashboard
import com.studyflow.app.presentation.viewmodel.*

@Composable
fun GeneralDashboardTab(state: StatisticsState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            RechartsVisualDashboard(
                timeStudiedTodayMinutes = state.timeStudiedToday,
                timeStudiedWeekMinutes = state.timeStudiedWeek,
                timeStudiedMonthMinutes = state.timeStudiedMonth,
                timeStudiedTotalMinutes = state.timeStudiedTotal,
                dailyGoalMinutes = state.dailyGoalMinutes,
                dailyGoalProgressPercent = state.dailyGoalProgressPercent,
                tasksCompletedToday = state.tasksCompletedToday,
                tasksTotalToday = state.tasksTotalToday,
                last7DaysHistory = state.last7DaysHistory
            )
        }

        if (state.insights.isNotEmpty()) {
            item {
                Text("Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                state.insights.forEach { insight ->
                    AppCard(containerColor = Color(0xFFF0FDF4), modifier = Modifier.padding(top = 8.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF166534))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(insight, color = Color(0xFF166534))
                        }
                    }
                }
            }
        }

        item {
            Text("Tempo Estudado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "Hoje", value = "${state.timeStudiedToday / 60}h ${state.timeStudiedToday % 60}m")
                StatCard(modifier = Modifier.weight(1f), title = "Semana", value = "${state.timeStudiedWeek / 60}h ${state.timeStudiedWeek % 60}m")
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "Mês", value = "${state.timeStudiedMonth / 60}h")
                StatCard(modifier = Modifier.weight(1f), title = "Total", value = "${state.timeStudiedTotal / 60}h")
            }
        }

        item {
            Text("Resumo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            AppCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResumoRow("Objetivo Atual", state.currentGoal?.title ?: "Nenhum")
                    ResumoRow("Dias até a prova", state.daysToExam?.toString() ?: "N/A")
                    ResumoRow("Próximas revisões", "${state.upcomingReviewsCount}")
                    ResumoRow("Próximas tarefas", "${state.upcomingTasksCount}")
                }
            }
        }
    }
}

@Composable
fun SubjectsTab(state: StatisticsState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(state.subjectStats) { stat ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stat.subject.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tempo: ${stat.timeStudied / 60}h ${stat.timeStudied % 60}m")
                        Text("Sessões: ${stat.sessionsCount}")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tarefas concluídas: ${stat.tasksCompleted}")
                        Text("Revisões: ${stat.reviewsCount}")
                    }
                    if (stat.performance > 0) {
                        Text("Desempenho: ${stat.performance.toInt()}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MethodsTab(state: StatisticsState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(state.methodStats) { stat ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stat.methodId.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        if (stat.isMostUsed) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("Mais Usada", modifier = Modifier.padding(horizontal = 4.dp)) }
                        }
                        if (stat.isMostEfficient) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(containerColor = Color(0xFF16A34A)) { Text("Mais Eficiente", modifier = Modifier.padding(horizontal = 4.dp)) }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tempo utilizado: ${stat.timeStudied / 60}h ${stat.timeStudied % 60}m")
                    Text("Sessões: ${stat.sessionsCount}")
                }
            }
        }
    }
}

@Composable
fun EssayStatsTab(state: StatisticsState) {
    if (state.essayStats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhuma redação avaliada ainda.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "Redações", value = "${state.essayStats.totalCount}")
                StatCard(modifier = Modifier.weight(1f), title = "Média", value = "${state.essayStats.averageScore.toInt()}")
                StatCard(modifier = Modifier.weight(1f), title = "Tempo Médio", value = "${state.essayStats.averageTime}m")
            }
        }
        item {
            Text("Média por Competência", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            AppCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompetenceBar("Competência 1", state.essayStats.c1Avg)
                    CompetenceBar("Competência 2", state.essayStats.c2Avg)
                    CompetenceBar("Competência 3", state.essayStats.c3Avg)
                    CompetenceBar("Competência 4", state.essayStats.c4Avg)
                    CompetenceBar("Competência 5", state.essayStats.c5Avg)
                }
            }
        }
    }
}

@Composable
fun SimulationStatsTab(state: StatisticsState) {
    if (state.simulationStats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum simulado realizado ainda.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "Simulados", value = "${state.simulationStats.totalCount}")
                StatCard(modifier = Modifier.weight(1f), title = "Média", value = "${state.simulationStats.averageScore.toInt()}")
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(modifier = Modifier.weight(1f), title = "Melhor Nota", value = "${state.simulationStats.bestScore.toInt()}")
                StatCard(modifier = Modifier.weight(1f), title = "Última Nota", value = "${state.simulationStats.lastScore.toInt()}")
            }
        }
        items(state.simulationStats.history) { sim ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(sim.name, fontWeight = FontWeight.Bold)
                    Text("Nota: ${sim.score.toInt()}", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun GoalsTab(state: StatisticsState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(state.goalStats) { goalStat ->
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(goalStat.goal.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(goalStat.goal.type, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { goalStat.progressPercent / 100f }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${goalStat.progressPercent.toInt()}% Concluído", style = MaterialTheme.typography.labelSmall)
                        if (goalStat.daysLeft != null) {
                            Text("${goalStat.daysLeft} dias restantes", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tempo investido: ${goalStat.timeInvested / 60}h", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String) {
    AppCard(modifier = modifier, containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ResumoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CompetenceBar(label: String, score: Float) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text("${score.toInt()}/200", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(progress = { score / 200f }, modifier = Modifier.fillMaxWidth())
    }
}
