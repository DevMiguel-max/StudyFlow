package com.studyflow.app.presentation.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyflow.app.data.local.StudyPlan
import com.studyflow.app.presentation.components.AppButton
import com.studyflow.app.presentation.viewmodel.StudyPlanViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlanFormScreen(
    navController: NavController,
    viewModel: StudyPlanViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    var name by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("Melhorar em uma matéria") }
    var availableHoursPerDay by remember { mutableStateOf("2.0") }
    var difficulty by remember { mutableStateOf(3f) }
    var priority by remember { mutableStateOf(3f) }
    
    var endDate by remember { mutableStateOf<Long?>(null) }
    
    // Days of week: 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
    var selectedDays by remember { mutableStateOf(setOf(2, 3, 4, 5, 6)) }
    var selectedSubjects by remember { mutableStateOf(setOf<Int>()) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    endDate?.let { calendar.timeInMillis = it }
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            endDate = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var expandedGoal by remember { mutableStateOf(false) }
    val goals = listOf(
        "Melhorar em uma matéria",
        "Preparar para uma prova escolar",
        "Preparar para ENEM",
        "Preparar para vestibular",
        "Aprender algo novo"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Plano de Estudo", fontWeight = FontWeight.Bold) },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Plano") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            ExposedDropdownMenuBox(
                expanded = expandedGoal,
                onExpandedChange = { expandedGoal = !expandedGoal }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = goal,
                    onValueChange = { },
                    label = { Text("Objetivo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGoal) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedGoal,
                    onDismissRequest = { expandedGoal = false }
                ) {
                    goals.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g) },
                            onClick = {
                                goal = g
                                expandedGoal = false
                            }
                        )
                    }
                }
            }

            OutlinedButton(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (endDate != null) "Data Final: ${dateFormat.format(Date(endDate!!))}" else "Definir Data Final")
            }
            
            OutlinedTextField(
                value = availableHoursPerDay,
                onValueChange = { availableHoursPerDay = it },
                label = { Text("Horas disponíveis por dia") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Dias de Estudo", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val days = listOf(1 to "D", 2 to "S", 3 to "T", 4 to "Q", 5 to "Q", 6 to "S", 7 to "S")
                days.forEach { (id, label) ->
                    FilterChip(
                        selected = selectedDays.contains(id),
                        onClick = {
                            selectedDays = if (selectedDays.contains(id)) selectedDays - id else selectedDays + id
                        },
                        label = { Text(label) }
                    )
                }
            }

            Text("Dificuldade Geral: ${difficulty.toInt()}", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = difficulty,
                onValueChange = { difficulty = it },
                valueRange = 1f..5f,
                steps = 3
            )

            Text("Prioridade: ${priority.toInt()}", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = priority,
                onValueChange = { priority = it },
                valueRange = 1f..5f,
                steps = 3
            )

            Text("Matérias Envolvidas", style = MaterialTheme.typography.labelLarge)
            if (state.subjects.isEmpty()) {
                Text("Nenhuma matéria cadastrada. Crie matérias primeiro.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            } else {
                state.subjects.forEach { subject ->
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = selectedSubjects.contains(subject.id),
                            onCheckedChange = { checked ->
                                selectedSubjects = if (checked) selectedSubjects + subject.id else selectedSubjects - subject.id
                            }
                        )
                        Text(subject.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AppButton(
                text = "Gerar Cronograma Inteligente",
                onClick = {
                    if (name.isNotBlank() && endDate != null && selectedSubjects.isNotEmpty()) {
                        val plan = StudyPlan(
                            name = name,
                            goal = goal,
                            subjectsInvolved = selectedSubjects.joinToString(","),
                            startDate = System.currentTimeMillis(),
                            endDate = endDate!!,
                            availableHoursPerDay = availableHoursPerDay.toFloatOrNull() ?: 2f,
                            availableDaysOfWeek = selectedDays.joinToString(","),
                            difficulty = difficulty.toInt(),
                            priority = priority.toInt()
                        )
                        viewModel.addStudyPlan(plan) {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
    }
}
