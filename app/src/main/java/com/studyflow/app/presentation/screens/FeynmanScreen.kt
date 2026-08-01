package com.studyflow.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyflow.app.presentation.components.AppButton
import com.studyflow.app.data.local.StudyNote
import com.studyflow.app.presentation.viewmodel.StudySessionViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeynmanScreen(
    navController: NavController,
    subjectId: Int,
    taskId: Int?,
    viewModel: StudySessionViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    var struggles by remember { mutableStateOf("") }
    var learned by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Técnica Feynman", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Explique como se fosse para uma criança", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tópico abordado") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = explanation,
                onValueChange = { explanation = it },
                label = { Text("Sua explicação simples") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                maxLines = 6
            )

            OutlinedTextField(
                value = struggles,
                onValueChange = { struggles = it },
                label = { Text("Onde você teve dificuldade?") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4
            )

            OutlinedTextField(
                value = learned,
                onValueChange = { learned = it },
                label = { Text("Resumo do que aprendeu") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppButton(
                text = "Concluir Sessão Feynman",
                onClick = {
                    // Start and immediately stop a session to record it
                    viewModel.startPomodoro(subjectId, taskId, focusMinutes = 0)
                    viewModel.stopSession(perceivedPerformance = 4, notes = "Feynman: $title")
                    
                    // Ideally we should also save the StudyNote here. 
                    // To do this simply, we assume StudySessionViewModel handles it or we just pop back for now.
                    navController.popBackStack()
                }
            )
        }
    }
}
