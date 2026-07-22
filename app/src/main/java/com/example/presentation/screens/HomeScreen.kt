package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.presentation.components.*
import com.example.presentation.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            AppTopBar(level = 12, streak = 14, xp = 1200)
        }
        
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Oi, Miguel! 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                val motivations = listOf(
                    "Pequenos passos todos os dias.",
                    "O esforço de hoje é o sucesso de amanhã.",
                    "Você está mais perto do que imagina.",
                    "Constância vence a intensidade.",
                    "Seu futuro agradece pelo seu estudo hoje."
                )
                val randomMotivation = remember { motivations.random() }
                
                Text(
                    text = randomMotivation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )

                Text(
                    text = "Você tem ${state.totalEstimatedTimeMinutes / 60}h ${state.totalEstimatedTimeMinutes % 60}m de tarefas no total.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AppCard(
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("⏳", style = MaterialTheme.typography.headlineMedium)
                            Text("Estudado Hoje", style = MaterialTheme.typography.labelSmall)
                            Text("${state.timeStudiedTodayMinutes} min", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    AppCard(
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("🏆", style = MaterialTheme.typography.headlineMedium)
                            Text("Sessões", style = MaterialTheme.typography.labelSmall)
                            Text("${state.totalSessionsCompleted}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                val timeProgress = if (state.estimatedTimeTodayMinutes > 0) (state.timeStudiedTodayMinutes.toFloat() / state.estimatedTimeTodayMinutes).coerceIn(0f, 1f) else 0f
                val timeText = if (state.estimatedTimeTodayMinutes > 0) "${state.timeStudiedTodayMinutes} / ${state.estimatedTimeTodayMinutes} min hoje" else "Sem metas de tempo hoje"
                ProgressCard(
                    title = "Meta do Dia",
                    progressText = timeText,
                    progress = timeProgress,
                    onContinueClick = {
                        val taskId = state.nextTask?.id ?: -1
                        val subjectId = state.nextTask?.subjectId ?: -1
                        val method = state.recommendedMethod?.id ?: "pomodoro"
                        
                        if (method == "feynman") {
                            navController.navigate("feynman?subjectId=$subjectId&taskId=$taskId")
                        } else {
                            navController.navigate("pomodoro?subjectId=$subjectId&taskId=$taskId")
                        }
                    }
                )
                
                if (state.upcomingExamDays != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AppCard(containerColor = Color(0xFFFEF3C7)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("⚠️ Próxima Prova em ${state.upcomingExamDays} dias", fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        }
                    }
                }
            }
        }
        
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionTitle(
                    title = "Explorar",
                    actionText = "",
                    onActionClick = {}
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModuleCard(title = "Objetivos", icon = "🎯", color = Color(0xFFE0F2FE), modifier = Modifier.weight(1f)) { navController.navigate("preparation") }
                    ModuleCard(title = "Cronograma", icon = "📅", color = Color(0xFFFCE7F3), modifier = Modifier.weight(1f)) { navController.navigate("calendar") }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModuleCard(title = "Redação", icon = "📝", color = Color(0xFFF3E8FF), modifier = Modifier.weight(1f)) { navController.navigate("essay_module") }
                    ModuleCard(title = "Simulados", icon = "📊", color = Color(0xFFD1FAE5), modifier = Modifier.weight(1f)) { navController.navigate("simulations") }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModuleCard(title = "Métodos", icon = "🧠", color = Color(0xFFFEF3C7), modifier = Modifier.weight(1f)) { navController.navigate("study_methods") }
                    ModuleCard(title = "Sessões", icon = "🏆", color = Color(0xFFE0E7FF), modifier = Modifier.weight(1f)) { navController.navigate("tasks") }
                }
            }
        }
        
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionTitle(
                    title = "Planos de Estudo Inteligentes",
                    actionText = "Criar Novo",
                    onActionClick = { navController.navigate("study_plan_form") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                if (state.activePlans.isEmpty()) {
                    EmptyState(icon = "🎯", title = "Sem planos", description = "Crie um plano inteligente para guiar seus estudos.")
                } else {
                    state.activePlans.forEach { plan ->
                        AppCard(containerColor = Color(0xFFF1F5F9), modifier = Modifier.padding(bottom = 8.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(plan.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(plan.goal, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                val daysLeft = ((plan.endDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                                Text("Faltam $daysLeft dias", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionTitle(
                    title = "Plano de Estudos de Hoje",
                    actionText = "Ver tudo",
                    onActionClick = { navController.navigate("tasks") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                if (state.todaysTasks.isEmpty()) {
                    EmptyState(icon = "✅", title = "Tudo livre", description = "Você não tem tarefas para hoje.")
                } else {
                    state.todaysTasks.forEach { task ->
                        AppCard(containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.padding(bottom = 8.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(if (task.type == "revision") "🔄 Revisão" else "📚 Estudo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text("${task.estimatedMinutes} min", style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(task.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                
                if (state.nextRevision != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AppCard(containerColor = Color(0xFFE0F2FE)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Próxima Revisão Agendada", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0369A1))
                            Text(state.nextRevision!!.title, fontWeight = FontWeight.Bold, color = Color(0xFF075985))
                            state.nextRevision!!.date?.let {
                                Text(dateFormat.format(Date(it)), style = MaterialTheme.typography.bodySmall, color = Color(0xFF0369A1))
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionTitle(
                    title = "Técnica Recomendada",
                    actionText = "Trocar",
                    onActionClick = { navController.navigate("study_methods") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                state.recommendedMethod?.let { method ->
                    MethodCard(
                        name = method.name,
                        description = "Ideal para a sua próxima tarefa",
                        icon = method.icon,
                        iconBgColor = method.color,
                        onClick = { navController.navigate("study_method_detail/${method.id}") }
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
