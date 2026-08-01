package com.studyflow.app.presentation.screens
import com.studyflow.app.presentation.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyflow.app.data.local.Subject
import com.studyflow.app.presentation.components.*
import com.studyflow.app.presentation.viewmodel.SubjectViewModel
import com.studyflow.app.presentation.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    navController: NavController,
    viewModel: SubjectViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }

    if (showDeleteDialog && subjectToDelete != null) {
        DeleteSubjectDialog(
            subject = subjectToDelete!!,
            availableSubjects = state.subjects,
            onDismiss = { showDeleteDialog = false; subjectToDelete = null },
            onConfirmDeleteAll = {
                viewModel.deleteSubject(subjectToDelete!!, deleteTasks = true, moveToSubjectId = null)
                showDeleteDialog = false
                subjectToDelete = null
            },
            onConfirmMoveTasks = { newSubjectId ->
                viewModel.deleteSubject(subjectToDelete!!, deleteTasks = false, moveToSubjectId = newSubjectId)
                showDeleteDialog = false
                subjectToDelete = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Matérias", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ModernFAB(
                onClick = { navController.navigate("subject_form") },
                icon = Icons.Default.Add,
                contentDescription = "Adicionar Matéria"
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Pesquisar matérias") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (state.subjects.isEmpty()) {
                EmptyState(icon = "📚", title = "Nenhuma matéria", description = "Adicione matérias para organizar seus estudos.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    val chunked = state.subjects.chunked(2)
                    items(chunked) { rowSubjects ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            rowSubjects.forEach { subject ->
                                Box(modifier = Modifier.weight(1f)) {
                                    SubjectCard(
                                        name = subject.name, 
                                        topic = subject.professor, 
                                        icon = subject.icon.ifBlank { "📚" },
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        iconBgColor = Color(0xFFD1FAE5), 
                                        iconColor = Color(0xFF059669), 
                                        borderColor = Color(0xFF10B981),
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            navController.navigate("subject_form?subjectId=${subject.id}")
                                        }
                                    )
                                    IconButton(
                                        onClick = { subjectToDelete = subject; showDeleteDialog = true },
                                        modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                                    ) {
                                        Text("🗑️")
                                    }
                                }
                            }
                            if (rowSubjects.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    navController: NavController,
    viewModel: TaskViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var expandedSubjectFilter by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tarefas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ModernFAB(
                onClick = { navController.navigate("task_form") },
                icon = Icons.Default.Add,
                contentDescription = "Adicionar Tarefa"
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Pesquisar tarefas") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple Filter Buttons row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Filter by completed
                FilterChip(
                    selected = state.filterState.completed == true,
                    onClick = { viewModel.onFilterCompletedChanged(if (state.filterState.completed == true) null else true) },
                    label = { Text("Concluídas") }
                )
                FilterChip(
                    selected = state.filterState.completed == false,
                    onClick = { viewModel.onFilterCompletedChanged(if (state.filterState.completed == false) null else false) },
                    label = { Text("Pendentes") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            if (state.tasks.isEmpty()) {
                EmptyState(icon = "✅", title = "Nenhuma tarefa", description = "Adicione tarefas para se manter em dia.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(state.tasks) { task ->
                        val subject = state.subjects.find { it.id == task.subjectId }
                        Box(modifier = Modifier.clickable { navController.navigate("task_form?taskId=${task.id}") }) {
                            TaskCard(
                                title = task.title, 
                                subject = subject?.name ?: "Sem matéria", 
                                time = "${task.estimatedMinutes} min", 
                                isCompleted = task.status == "completed", 
                                onToggle = { viewModel.toggleTaskStatus(task) }
                            )
                            IconButton(
                                onClick = { viewModel.deleteTask(task) },
                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterEnd).padding(end = 8.dp)
                            ) {
                                Text("🗑️")
                            }
                        }
                    }
                }
            }
        }
    }
}



