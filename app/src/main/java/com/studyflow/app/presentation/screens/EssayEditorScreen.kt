package com.studyflow.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyflow.app.presentation.components.AppButton
import com.studyflow.app.presentation.viewmodel.EssayViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EssayEditorScreen(
    navController: NavController,
    submissionId: Int,
    themeId: Int,
    viewModel: EssayViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    var text by remember { mutableStateOf("") }
    var timeSpentSeconds by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var showMotivationalTexts by remember { mutableStateOf(false) }

    LaunchedEffect(submissionId, themeId) {
        if (submissionId != -1) {
            viewModel.loadSubmission(submissionId)
        } else {
            viewModel.selectTheme(themeId)
        }
    }

    LaunchedEffect(state.currentSubmission) {
        if (state.currentSubmission != null && text.isEmpty()) {
            text = state.currentSubmission!!.text
            timeSpentSeconds = state.currentSubmission!!.timeSpentSeconds
            if (state.currentSubmission!!.status != "draft") {
                isTimerRunning = false
            }
        }
    }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            timeSpentSeconds++
            // Auto save every 30 seconds
            if (timeSpentSeconds % 30 == 0) {
                viewModel.saveSubmission(text, timeSpentSeconds, "draft")
            }
        }
    }

    val theme = state.currentTheme
    val isReadOnly = state.currentSubmission?.status == "graded"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (!isReadOnly) viewModel.saveSubmission(text, timeSpentSeconds, "draft")
                        navController.popBackStack() 
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (!isReadOnly) {
                        IconButton(onClick = { 
                            viewModel.submitForCorrection(text, timeSpentSeconds)
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Enviar para Correção")
                        }
                    } else {
                        TextButton(onClick = { navController.navigate("essay_result/${state.currentSubmission?.id}") }) {
                            Text("Ver Correção")
                        }
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
        ) {
            if (theme != null) {
                Text(theme.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tempo: ${String.format("%02d:%02d", timeSpentSeconds / 60, timeSpentSeconds % 60)}", style = MaterialTheme.typography.labelSmall)
                val wordCount = text.split("\\s+".toRegex()).count { it.isNotBlank() }
                Text("$wordCount palavras", style = MaterialTheme.typography.labelSmall)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (state.currentMotivationalTexts.isNotEmpty()) {
                AppButton(
                    text = "Ver Textos Motivadores",
                    onClick = { showMotivationalTexts = true },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = text,
                onValueChange = { if (!isReadOnly) text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Comece a escrever sua redação aqui...") },
                readOnly = isReadOnly
            )
        }

        if (showMotivationalTexts) {
            ModalBottomSheet(onDismissRequest = { showMotivationalTexts = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 32.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Textos Motivadores", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    state.currentMotivationalTexts.forEach { mt ->
                        Text(mt.title, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(mt.content, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Fonte: ${mt.source}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
