package com.studyflow.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.studyflow.app.presentation.viewmodel.MentorViewModel
import com.studyflow.app.data.local.MentorProfile
import com.studyflow.app.data.local.MentorRecommendation
import com.studyflow.app.data.local.SmartMission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorScreen(
    onNavigateBack: () -> Unit,
    viewModel: MentorViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(0) }
    val tabs = listOf("Painel", "Perfil", "Missões")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mentor Inteligente") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (!uiState.isLoading) {
                        IconButton(onClick = { viewModel.runAnalysis() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Analisar Progresso")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(selectedTabIndex = currentTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            if (uiState.error != null) {
                Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(16.dp))
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (currentTab) {
                    0 -> MentorDashboardTab(
                        recommendations = uiState.recommendations,
                        lastAssessment = uiState.lastAssessment,
                        onMarkRead = { viewModel.markRecommendationAsRead(it) }
                    )
                    1 -> MentorProfileTab(
                        profile = uiState.profile,
                        onProfileUpdate = { viewModel.updateProfile(it) }
                    )
                    2 -> MentorMissionsTab(
                        missions = uiState.activeMissions,
                        onComplete = { viewModel.completeMission(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun MentorDashboardTab(
    recommendations: List<MentorRecommendation>,
    lastAssessment: String?,
    onMarkRead: (MentorRecommendation) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            if (lastAssessment != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Avaliação Geral", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(lastAssessment, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        
        item {
            Text("Recomendações", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
        }
        
        if (recommendations.isEmpty()) {
            item {
                Text("Nenhuma recomendação no momento. Clique no botão atualizar para analisar seu progresso.")
            }
        } else {
            items(recommendations) { rec ->
                RecommendationCard(rec, onMarkRead)
            }
        }
    }
}

@Composable
fun RecommendationCard(rec: MentorRecommendation, onMarkRead: (MentorRecommendation) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when(rec.type) {
                    "Warning" -> Icons.Default.Warning
                    "Motivation" -> Icons.Default.Star
                    "Schedule" -> Icons.Default.DateRange
                    else -> Icons.Default.Info
                }
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(rec.type, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.weight(1f))
                if (!rec.isRead) {
                    Badge { Text("Novo") }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(rec.text, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Motivo: ${rec.reason}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            if (!rec.isRead) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onMarkRead(rec) }, modifier = Modifier.align(Alignment.End)) {
                    Text("Marcar como lida")
                }
            }
        }
    }
}

@Composable
fun MentorProfileTab(
    profile: MentorProfile,
    onProfileUpdate: (MentorProfile) -> Unit
) {
    var mainGoal by remember { mutableStateOf(profile.mainGoal) }
    var examType by remember { mutableStateOf(profile.examType) }
    var hoursPerDay by remember { mutableStateOf(profile.hoursPerDay.toString()) }
    var favoriteSubjects by remember { mutableStateOf(profile.favoriteSubjects) }
    var difficultSubjects by remember { mutableStateOf(profile.difficultSubjects) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Perfil de Aprendizagem", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
            
            OutlinedTextField(
                value = mainGoal,
                onValueChange = { mainGoal = it },
                label = { Text("Objetivo Principal (ex: Polícia Federal)") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = examType,
                onValueChange = { examType = it },
                label = { Text("Tipo de Prova (ex: Concurso)") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = hoursPerDay,
                onValueChange = { hoursPerDay = it },
                label = { Text("Horas disponíveis por dia") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = favoriteSubjects,
                onValueChange = { favoriteSubjects = it },
                label = { Text("Disciplinas Favoritas") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = difficultSubjects,
                onValueChange = { difficultSubjects = it },
                label = { Text("Disciplinas com Dificuldade") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
            
            Button(
                onClick = {
                    onProfileUpdate(
                        profile.copy(
                            mainGoal = mainGoal,
                            examType = examType,
                            hoursPerDay = hoursPerDay.toIntOrNull() ?: 4,
                            favoriteSubjects = favoriteSubjects,
                            difficultSubjects = difficultSubjects
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar Perfil")
            }
        }
    }
}

@Composable
fun MentorMissionsTab(
    missions: List<SmartMission>,
    onComplete: (SmartMission) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Missões Inteligentes", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
        }
        
        if (missions.isEmpty()) {
            item {
                Text("Nenhuma missão ativa. O Mentor gerará missões baseadas no seu perfil.")
            }
        } else {
            items(missions) { mission ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(mission.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                                Text("+${mission.xpReward} XP")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(mission.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { onComplete(mission) }, modifier = Modifier.align(Alignment.End)) {
                            Text("Completar")
                        }
                    }
                }
            }
        }
    }
}
