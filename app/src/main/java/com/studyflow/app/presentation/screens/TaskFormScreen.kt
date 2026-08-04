package com.studyflow.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyflow.app.data.local.Subject
import com.studyflow.app.data.local.Task
import com.studyflow.app.presentation.components.AppButton
import com.studyflow.app.presentation.components.AppCard
import com.studyflow.app.presentation.viewmodel.TaskViewModel
import org.koin.androidx.compose.koinViewModel

import com.studyflow.app.data.local.StudyMethodsData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    navController: NavController,
    taskId: Int? = null,
    viewModel: TaskViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val existingTask = remember(state.tasks, taskId) {
        state.tasks.find { it.id == taskId }
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subjectId by remember { mutableStateOf<Int?>(null) }
    var priority by remember { mutableStateOf(1f) } // 1=Low, 2=Medium, 3=High
    var estimatedMinutes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(existingTask) {
        existingTask?.let {
            title = it.title
            description = it.description
            subjectId = it.subjectId
            priority = it.priority.toFloat()
            estimatedMinutes = it.estimatedMinutes.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) "Nova Tarefa" else "Detalhes da Tarefa", fontWeight = FontWeight.Bold) },
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
            
            existingTask?.recommendedMethodId?.let { methodId ->
                val method = StudyMethodsData.methods.find { it.id == methodId }
                if (method != null) {
                    AppCard(containerColor = method.color) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("💡 Técnica Recomendada", style = MaterialTheme.typography.labelSmall)
                            Text(method.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Tempo sugerido: ${existingTask.estimatedMinutes} min", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { navController.navigate("study_method_detail/${method.id}") }) {
                                Text("Aprender técnica")
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título da Tarefa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth()
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = state.subjects.find { it.id == subjectId }?.name ?: "Selecione uma Matéria",
                    onValueChange = { },
                    label = { Text("Matéria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    state.subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject.name) },
                            onClick = {
                                subjectId = subject.id
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = estimatedMinutes,
                onValueChange = { estimatedMinutes = it },
                label = { Text("Tempo Estimado (minutos)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Text("Prioridade: ${when(priority.toInt()) { 1 -> "Baixa" 2 -> "Média" else -> "Alta" }}", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = priority,
                onValueChange = { priority = it },
                valueRange = 1f..3f,
                steps = 1
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            AppButton(
                text = "Salvar Tarefa",
                onClick = {
                    if (title.isNotBlank() && subjectId != null) {
                        val newTask = Task(
                            id = taskId ?: 0,
                            title = title,
                            description = description,
                            subjectId = subjectId!!,
                            priority = priority.toInt(),
                            estimatedMinutes = estimatedMinutes.toIntOrNull() ?: 0,
                            status = existingTask?.status ?: "pending"
                        )
                        if (taskId == null) {
                            viewModel.addTask(newTask)
                        } else {
                            viewModel.updateTask(newTask)
                        }
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
