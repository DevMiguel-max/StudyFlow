package com.studyflow.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.studyflow.app.data.local.StudyMethodModel
import com.studyflow.app.data.local.StudyMethodsData
import com.studyflow.app.presentation.components.AppButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMethodDetailScreen(navController: NavController, methodId: String) {
    val method = StudyMethodsData.methods.find { it.id == methodId }

    if (method == null) {
        // Fallback
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(method.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    AppButton(
                        text = "Começar Sessão de Estudo",
                        onClick = {
                            when (method.id) {
                                "feynman" -> navController.navigate("feynman?subjectId=-1&taskId=-1")
                                "spaced_repetition", "leitner" -> navController.navigate("flashcard?subjectId=-1")
                                "cornell" -> navController.navigate("cornell?subjectId=-1")
                                "blurting" -> navController.navigate("blurting?subjectId=-1")
                                "sq3r" -> navController.navigate("sq3r?subjectId=-1")
                                "time_blocking" -> navController.navigate("time_blocking")
                                else -> navController.navigate("pomodoro?subjectId=-1&taskId=-1")
                            }
                        },
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(method.color)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(method.icon, style = MaterialTheme.typography.displayMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(method.goal, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                
                Section("Como Funciona") {
                    Text(method.howItWorks, style = MaterialTheme.typography.bodyMedium)
                }

                Section("Passo a Passo") {
                    method.stepByStep.forEachIndexed { index, step ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(step, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Section("Quando Utilizar") {
                    Text(method.whenToUse, style = MaterialTheme.typography.bodyMedium)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vantagens", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.height(8.dp))
                        method.pros.forEach { pro ->
                            Text("• $pro", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Desvantagens", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.height(8.dp))
                        method.cons.forEach { con ->
                            Text("• $con", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }

                Section("Dicas") {
                    method.tips.forEach { tip ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("💡", modifier = Modifier.padding(end = 8.dp))
                            Text(tip, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                
                Section("Erros Comuns") {
                    method.commonMistakes.forEach { mistake ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("❌", modifier = Modifier.padding(end = 8.dp))
                            Text(mistake, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Section("História e Criador") {
                    Text("Criado por: ${method.creator}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(method.history, style = MaterialTheme.typography.bodyMedium)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}
