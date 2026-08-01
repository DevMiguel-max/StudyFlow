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
import com.studyflow.app.data.local.StudyGoal
import com.studyflow.app.presentation.components.AppButton
import com.studyflow.app.domain.repository.StudyFlowRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExamGoalFormScreen(
    navController: NavController,
    repository: StudyFlowRepository = koinInject()
) {
    var type by remember { mutableStateOf("ENEM") }
    var title by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var targetScore by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Objetivo", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Tipo de Objetivo", style = MaterialTheme.typography.labelMedium)
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf("Escola", "ENEM", "Vestibular", "Concurso", "Faculdade", "Certificação", "Idioma", "Personalizado")
                types.forEach { t ->
                    FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                }
            }

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título (Ex: ENEM 2024)") }, modifier = Modifier.fillMaxWidth())
            
            if (type == "ENEM" || type == "Vestibular" || type == "Faculdade") {
                OutlinedTextField(value = course, onValueChange = { course = it }, label = { Text("Curso Desejado") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = targetScore, onValueChange = { targetScore = it }, label = { Text("Meta de Nota") }, modifier = Modifier.fillMaxWidth())
            }

            if (type == "Concurso") {
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Cargo (Ex: Analista)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = institution, onValueChange = { institution = it }, label = { Text("Órgão (Ex: Banco do Brasil)") }, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(32.dp))

            AppButton(text = "Salvar Objetivo", onClick = {
                coroutineScope.launch {
                    val goal = StudyGoal(
                        type = type,
                        title = title.ifBlank { "Novo Objetivo" },
                        course = course.takeIf { it.isNotBlank() },
                        role = role.takeIf { it.isNotBlank() },
                        institution = institution.takeIf { it.isNotBlank() },
                        targetScore = targetScore.toFloatOrNull(),
                        date = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000 // Mock 30 days ahead
                    )
                    repository.insertStudyGoal(goal)
                    navController.popBackStack()
                }
            })
        }
    }
}
