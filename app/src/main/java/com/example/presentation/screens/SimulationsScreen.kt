package com.example.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.presentation.components.AppButton
import com.example.presentation.components.AppCard
import com.example.presentation.viewmodel.SimulationViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationsScreen(
    navController: NavController,
    viewModel: SimulationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulados", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Resultado")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (state.simulations.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nenhum simulado registrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(state.simulations) { simulation ->
                AppCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(simulation.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(dateFormat.format(Date(simulation.date)), style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(simulation.examType, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Acertos", style = MaterialTheme.typography.labelSmall)
                                Text("${simulation.correctAnswers}/${simulation.totalQuestions}", fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Erros", style = MaterialTheme.typography.labelSmall)
                                Text("${simulation.wrongAnswers}", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Nota", style = MaterialTheme.typography.labelSmall)
                                Text(String.format("%.1f", simulation.score), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddSimulationDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, type, total, correct ->
                    viewModel.addSimulation(name, type, total, correct)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddSimulationDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("ENEM") }
    var totalQuestions by remember { mutableStateOf("") }
    var correctAnswers by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Simulado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome (Ex: Simulado SAS)") })
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo (ENEM, FUVEST)") })
                OutlinedTextField(value = totalQuestions, onValueChange = { totalQuestions = it }, label = { Text("Total de Questões") })
                OutlinedTextField(value = correctAnswers, onValueChange = { correctAnswers = it }, label = { Text("Acertos") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val total = totalQuestions.toIntOrNull() ?: 0
                val correct = correctAnswers.toIntOrNull() ?: 0
                if (name.isNotBlank() && total > 0 && correct in 0..total) {
                    onAdd(name, type, total, correct)
                }
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
