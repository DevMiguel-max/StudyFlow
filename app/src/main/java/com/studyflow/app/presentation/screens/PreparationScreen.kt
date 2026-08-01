package com.studyflow.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyflow.app.presentation.components.AppCard
import com.studyflow.app.presentation.components.EmptyState
import com.studyflow.app.presentation.components.ModuleCard
import com.studyflow.app.presentation.viewmodel.PreparationViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreparationScreen(
    navController: NavController,
    viewModel: PreparationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Objetivos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Objective Card
            if (state.currentGoal != null) {
                AppCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Objetivo Atual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(state.currentGoal!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Faltam ${state.upcomingExamDays} dias", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Meta: ${state.currentGoal!!.targetScore ?: "N/A"}")
                        }
                    }
                }
            } else {
                AppCard(containerColor = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.clickable { navController.navigate("exam_goal_form") }) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎯 Nenhum objetivo definido", fontWeight = FontWeight.Bold)
                        Text("Toque para configurar sua próxima prova", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AppCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏳", style = MaterialTheme.typography.headlineMedium)
                        Text("${state.totalHoursStudied}h", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Estudadas", style = MaterialTheme.typography.labelSmall)
                    }
                }
                AppCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📝", style = MaterialTheme.typography.headlineMedium)
                        Text("${state.essaysCompleted}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Redações", style = MaterialTheme.typography.labelSmall)
                    }
                }
                AppCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", style = MaterialTheme.typography.headlineMedium)
                        Text("${state.simulationsCompleted}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("Simulados", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Modules
            Text("Módulos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ModuleCard(
                    title = "ENEM", icon = "🎯", color = Color(0xFFE0F2FE), modifier = Modifier.weight(1f)
                ) { navController.navigate("enem") }
                
                ModuleCard(
                    title = "Vestibulares", icon = "🏛️", color = Color(0xFFFEF3C7), modifier = Modifier.weight(1f)
                ) { navController.navigate("vestibulares") }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ModuleCard(
                    title = "Redação", icon = "📝", color = Color(0xFFF3E8FF), modifier = Modifier.weight(1f)
                ) { navController.navigate("essay_module") }
                
                ModuleCard(
                    title = "Simulados", icon = "📊", color = Color(0xFFD1FAE5), modifier = Modifier.weight(1f)
                ) { navController.navigate("simulations") }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ModuleCard(
                    title = "Concursos", icon = "🏢", color = Color(0xFFFCE7F3), modifier = Modifier.weight(1f)
                ) { navController.navigate("concursos") }
                
                ModuleCard(
                    title = "Faculdade", icon = "🎓", color = Color(0xFFE0E7FF), modifier = Modifier.weight(1f)
                ) { navController.navigate("faculdade") }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ModuleCard(
                    title = "Análise de Documentos", icon = "📄", color = Color(0xFFFFF7ED), modifier = Modifier.weight(1f)
                ) { navController.navigate("editais") }
                ModuleCard(title = "Gerador de Materiais", icon = "✨", color = androidx.compose.ui.graphics.Color(0xFFE0E7FF), modifier = Modifier.weight(1f)) { navController.navigate("material_generator") }
            }
        }
    }
}
