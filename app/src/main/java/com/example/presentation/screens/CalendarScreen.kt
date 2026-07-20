package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.presentation.components.AppCard
import com.example.presentation.components.EmptyState
import com.example.presentation.components.TaskCard
import com.example.presentation.viewmodel.CalendarViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    
    // Generate 30 days starting from 15 days ago
    calendar.add(Calendar.DAY_OF_YEAR, -15)
    val days = (0..30).map { 
        val time = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        time
    }

    val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            
            Text(
                text = monthFormat.format(Date(state.selectedDateMillis)).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Horizontal calendar
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(days) { dayMillis ->
                    val isSelected = isSameDay(dayMillis, state.selectedDateMillis)
                    val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val textCol = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    
                    Column(
                        modifier = Modifier
                            .width(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bg)
                            .clickable { viewModel.selectDate(dayMillis) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(dayOfWeekFormat.format(Date(dayMillis)).take(3).uppercase(), style = MaterialTheme.typography.labelSmall, color = textCol.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(dayFormat.format(Date(dayMillis)), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textCol)
                        
                        // Indicators (just simple dots)
                        val hasTasks = state.tasks.any { isSameDay(it.date ?: 0L, dayMillis) }
                        if (hasTasks) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.size(4.dp).clip(RoundedCornerShape(50)).background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Selected Day Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.examsForSelectedDate.isNotEmpty()) {
                    item {
                        Text("Provas", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    items(state.examsForSelectedDate) { exam ->
                        AppCard(containerColor = Color(0xFFFEF3C7)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("⚠️ Prova de ${exam.name}", fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                Text(exam.examContent, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB45309))
                            }
                        }
                    }
                }
                
                if (state.tasksForSelectedDate.isNotEmpty()) {
                    val pending = state.tasksForSelectedDate.filter { it.status != "completed" }
                    val completed = state.tasksForSelectedDate.filter { it.status == "completed" }
                    
                    if (pending.isNotEmpty()) {
                        item {
                            Text("Tarefas Pendentes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        items(pending) { task ->
                            val subject = state.subjects.find { it.id == task.subjectId }
                            Box(modifier = Modifier.clickable { navController.navigate("task_form?taskId=${task.id}") }) {
                                TaskCard(
                                    title = task.title, 
                                    subject = subject?.name ?: "Sem matéria", 
                                    time = "${task.estimatedMinutes} min", 
                                    isCompleted = false, 
                                    onToggle = { /* Not directly toggling here, or could add toggle action in viewModel */ }
                                )
                            }
                        }
                    }
                    
                    if (completed.isNotEmpty()) {
                        item {
                            Text("Tarefas Concluídas", style = MaterialTheme.typography.titleSmall, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                        items(completed) { task ->
                            val subject = state.subjects.find { it.id == task.subjectId }
                            TaskCard(
                                title = task.title, 
                                subject = subject?.name ?: "Sem matéria", 
                                time = "${task.estimatedMinutes} min", 
                                isCompleted = true, 
                                onToggle = { }
                            )
                        }
                    }
                } else if (state.examsForSelectedDate.isEmpty()) {
                    item {
                        EmptyState(icon = "🏖️", title = "Dia livre", description = "Você não tem atividades agendadas para este dia.")
                    }
                }
            }
        }
    }
}

fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
