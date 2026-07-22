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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.presentation.components.AppCard
import com.example.presentation.components.EmptyState
import com.example.presentation.components.TaskCard
import com.example.presentation.viewmodel.CalendarViewModel
import com.example.presentation.viewmodel.EventType
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
                        
                        // Indicators
                        val dayStart = getStartOfDay(dayMillis)
                        val types = state.daysWithEvents[dayStart] ?: emptyList()
                        
                        if (types.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                types.take(3).forEach { type ->
                                    val color = when (type) {
                                        EventType.EXAM -> Color.Red
                                        EventType.GOAL -> Color(0xFFD97706)
                                        EventType.SIMULATION -> Color(0xFF8B5CF6)
                                        EventType.ESSAY -> Color(0xFFEC4899)
                                        EventType.REVIEW -> Color(0xFF3B82F6)
                                        EventType.SESSION -> Color(0xFF10B981)
                                        EventType.TASK -> Color.Gray
                                    }
                                    Box(modifier = Modifier.size(4.dp).clip(RoundedCornerShape(50)).background(color))
                                }
                            }
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
                if (state.eventsForSelectedDate.isEmpty()) {
                    item {
                        EmptyState(icon = "🏖️", title = "Dia livre", description = "Você não tem atividades agendadas para este dia.")
                    }
                } else {
                    val groupedEvents = state.eventsForSelectedDate.groupBy { it.type }
                    
                    // High priority first (Exams, Goals, Simulations)
                    listOf(EventType.EXAM, EventType.GOAL, EventType.SIMULATION).forEach { type ->
                        val evts = groupedEvents[type]
                        if (!evts.isNullOrEmpty()) {
                            items(evts) { ev ->
                                EventCard(ev)
                            }
                        }
                    }

                    // Medium priority (Essays, Reviews)
                    listOf(EventType.ESSAY, EventType.REVIEW).forEach { type ->
                        val evts = groupedEvents[type]
                        if (!evts.isNullOrEmpty()) {
                            items(evts) { ev ->
                                EventCard(ev)
                            }
                        }
                    }
                    
                    // Low priority (Tasks, Sessions)
                    listOf(EventType.TASK, EventType.SESSION).forEach { type ->
                        val evts = groupedEvents[type]
                        if (!evts.isNullOrEmpty()) {
                            items(evts) { ev ->
                                EventCard(ev)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: com.example.presentation.viewmodel.CalendarEvent) {
    val color = when (event.type) {
        EventType.EXAM -> Color(0xFFFEE2E2)
        EventType.GOAL -> Color(0xFFFEF3C7)
        EventType.SIMULATION -> Color(0xFFEDE9FE)
        EventType.ESSAY -> Color(0xFFFCE7F3)
        EventType.REVIEW -> Color(0xFFDBEAFE)
        EventType.SESSION -> Color(0xFFD1FAE5)
        EventType.TASK -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when (event.type) {
        EventType.EXAM -> Color(0xFFDC2626)
        EventType.GOAL -> Color(0xFFD97706)
        EventType.SIMULATION -> Color(0xFF8B5CF6)
        EventType.ESSAY -> Color(0xFFEC4899)
        EventType.REVIEW -> Color(0xFF2563EB)
        EventType.SESSION -> Color(0xFF059669)
        EventType.TASK -> MaterialTheme.colorScheme.onSurface
    }
    
    AppCard(containerColor = color) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(event.title, fontWeight = FontWeight.Bold, color = contentColor)
                Text(if (event.isCompleted) "Concluído" else "Pendente", style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.7f))
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

fun getStartOfDay(time: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = time
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
