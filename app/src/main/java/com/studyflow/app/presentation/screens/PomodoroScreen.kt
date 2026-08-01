package com.studyflow.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.studyflow.app.presentation.viewmodel.StudySessionViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    navController: NavController,
    subjectId: Int,
    taskId: Int?,
    viewModel: StudySessionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    var showFinishDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val phaseText = when (state.currentPhase) {
                "focus" -> "Tempo de Foco"
                "short_break" -> "Pausa Curta"
                "long_break" -> "Pausa Longa"
                else -> ""
            }
            
            Text(
                text = phaseText,
                style = MaterialTheme.typography.headlineSmall,
                color = if (state.currentPhase == "focus") MaterialTheme.colorScheme.primary else Color(0xFF10B981)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ciclo ${state.cyclesCompleted + 1}", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(48.dp))

            // Timer Display
            val minutes = state.timeRemainingSeconds / 60
            val seconds = state.timeRemainingSeconds % 60
            val timeString = String.format("%02d:%02d", minutes, seconds)

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(if (state.currentPhase == "focus") MaterialTheme.colorScheme.primaryContainer else Color(0xFFD1FAE5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeString,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.currentPhase == "focus") MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF065F46)
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                if (state.isRunning) {
                    FloatingActionButton(
                        onClick = { viewModel.pauseTimer() },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text("⏸️", fontSize = 24.sp)
                    }
                } else {
                    FloatingActionButton(
                        onClick = { viewModel.startPomodoro(subjectId, taskId) },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                FloatingActionButton(
                    onClick = { showFinishDialog = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Finalizar", tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = { showFinishDialog = false },
                title = { Text("Finalizar Sessão") },
                text = { Text("Deseja finalizar esta sessão de estudos e salvar seu progresso?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.stopSession()
                        showFinishDialog = false
                        navController.popBackStack()
                    }) {
                        Text("Sim, finalizar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFinishDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
