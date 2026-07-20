package com.example.presentation.screens

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
import com.example.data.local.Subject
import com.example.presentation.components.AppButton
import com.example.presentation.viewmodel.SubjectViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.*
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectFormScreen(
    navController: NavController,
    subjectId: Int? = null,
    viewModel: SubjectViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    val existingSubject = remember(state.subjects, subjectId) {
        state.subjects.find { it.id == subjectId }
    }

    var name by remember { mutableStateOf("") }
    var professor by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("📚") }
    var difficulty by remember { mutableStateOf(1f) }
    var examContent by remember { mutableStateOf("") }
    var availableHoursPerDay by remember { mutableStateOf("2.0") }
    var studyGoal by remember { mutableStateOf("Aprendizado") }
    
    var examDate by remember { mutableStateOf<Long?>(null) }
    
    // Days of week: 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
    var selectedDays by remember { mutableStateOf(setOf(2, 3, 4, 5, 6)) }

    LaunchedEffect(existingSubject) {
        existingSubject?.let {
            name = it.name
            professor = it.professor
            icon = it.icon
            difficulty = it.difficulty.toFloat()
            examContent = it.examContent
            availableHoursPerDay = it.availableHoursPerDay.toString()
            studyGoal = it.studyGoal
            examDate = it.examDate
            if (it.availableDaysOfWeek.isNotBlank()) {
                selectedDays = it.availableDaysOfWeek.split(",").mapNotNull { d -> d.toIntOrNull() }.toSet()
            }
        }
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    examDate?.let { calendar.timeInMillis = it }
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            examDate = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var expandedGoal by remember { mutableStateOf(false) }
    val goals = listOf("Prova", "Concurso", "Vestibular", "Faculdade", "Aprendizado")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (subjectId == null) "Nova Matéria" else "Editar Matéria", fontWeight = FontWeight.Bold) },
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
                label = { Text("Nome da Matéria") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = icon,
                onValueChange = { icon = it },
                label = { Text("Ícone (Emoji)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Dificuldade: ${difficulty.toInt()}", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = difficulty,
                onValueChange = { difficulty = it },
                valueRange = 1f..5f,
                steps = 3
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Plano de Estudos Inteligente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = examContent,
                onValueChange = { examContent = it },
                label = { Text("Conteúdo da Prova (separe por vírgulas)") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Ex: Células, Tecidos, Órgãos") }
            )
            
            OutlinedButton(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (examDate != null) "Data da Prova: ${dateFormat.format(Date(examDate!!))}" else "Definir Data da Prova")
            }
            
            OutlinedTextField(
                value = availableHoursPerDay,
                onValueChange = { availableHoursPerDay = it },
                label = { Text("Horas disponíveis por dia") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            ExposedDropdownMenuBox(
                expanded = expandedGoal,
                onExpandedChange = { expandedGoal = !expandedGoal }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = studyGoal,
                    onValueChange = { },
                    label = { Text("Objetivo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGoal) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedGoal,
                    onDismissRequest = { expandedGoal = false }
                ) {
                    goals.forEach { goal ->
                        DropdownMenuItem(
                            text = { Text(goal) },
                            onClick = {
                                studyGoal = goal
                                expandedGoal = false
                            }
                        )
                    }
                }
            }

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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AppButton(
                text = "Salvar e Gerar Plano",
                onClick = {
                    if (name.isNotBlank()) {
                        val newSubject = Subject(
                            id = subjectId ?: 0,
                            name = name,
                            professor = professor,
                            icon = icon,
                            difficulty = difficulty.toInt(),
                            examContent = examContent,
                            availableHoursPerDay = availableHoursPerDay.toFloatOrNull() ?: 2f,
                            availableDaysOfWeek = selectedDays.joinToString(","),
                            studyGoal = studyGoal,
                            examDate = examDate
                        )
                        if (subjectId == null) {
                            viewModel.addSubjectWithPlan(newSubject)
                        } else {
                            viewModel.updateSubjectWithPlan(newSubject)
                        }
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
