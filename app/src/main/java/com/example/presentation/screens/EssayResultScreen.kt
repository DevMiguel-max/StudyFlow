package com.example.presentation.screens

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
                title = { Text("Resultado da Correção", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (correction == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("Nota Final", style = MaterialTheme.typography.titleMedium)
                        Text("${correction.totalScore}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Text("Competências ENEM", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                CompetenceRow("C1: Norma Culta", correction.comp1)
                CompetenceRow("C2: Compreensão e Tema", correction.comp2)
                CompetenceRow("C3: Organização de Ideias", correction.comp3)
                CompetenceRow("C4: Coesão", correction.comp4)
                CompetenceRow("C5: Proposta de Intervenção", correction.comp5)

                Spacer(modifier = Modifier.height(16.dp))

                AppCard(containerColor = Color(0xFFD1FAE5)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pontos Fortes", fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(correction.strengths, color = Color(0xFF065F46))
                    }
                }

                AppCard(containerColor = Color(0xFFFEE2E2)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pontos a Melhorar", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(correction.weaknesses, color = Color(0xFF991B1B))
                    }
                }

                AppCard(containerColor = Color(0xFFFEF3C7)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Sugestões", fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(correction.suggestions, color = Color(0xFF92400E))
                    }
                }
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
