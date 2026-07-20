package com.example.presentation.screens

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
import com.example.presentation.components.AppButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CornellScreen(navController: NavController, subjectId: Int) {
    var notes by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Notas de Cornell") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "") } }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f, fill = false), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = keywords, onValueChange = { keywords = it }, label = { Text("Palavras-chave / Dúvidas") }, modifier = Modifier.weight(0.3f).height(300.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Anotações Principais") }, modifier = Modifier.weight(0.7f).height(300.dp))
            }
            OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Resumo") }, modifier = Modifier.fillMaxWidth().height(150.dp))
            AppButton(text = "Salvar", onClick = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlurtingScreen(navController: NavController, subjectId: Int) {
    var blurting by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Blurting") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "") } }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Escreva absolutamente tudo que você lembra sobre o tópico sem consultar nenhum material.", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(value = blurting, onValueChange = { blurting = it }, modifier = Modifier.fillMaxWidth().weight(1f), placeholder = { Text("Comece a escrever...") })
            AppButton(text = "Finalizar e Comparar", onClick = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SQ3RScreen(navController: NavController, subjectId: Int) {
    var step by remember { mutableStateOf(1) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Guia SQ3R") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "") } }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (step) {
                1 -> { Text("1. Survey (Pesquisar)", style = MaterialTheme.typography.titleLarge); Text("Leia títulos, subtítulos e resumos do capítulo.") }
                2 -> { Text("2. Question (Questionar)", style = MaterialTheme.typography.titleLarge); Text("Transforme os títulos em perguntas.") }
                3 -> { Text("3. Read (Ler)", style = MaterialTheme.typography.titleLarge); Text("Leia ativamente buscando responder às perguntas.") }
                4 -> { Text("4. Recite (Recitar)", style = MaterialTheme.typography.titleLarge); Text("Explique em voz alta o que acabou de ler.") }
                5 -> { Text("5. Review (Revisar)", style = MaterialTheme.typography.titleLarge); Text("Revise suas anotações e as perguntas.") }
            }
            Spacer(modifier = Modifier.weight(1f))
            AppButton(text = if (step < 5) "Próximo" else "Concluir", onClick = { if (step < 5) step++ else navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeBlockingScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Time Blocking") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "") } }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Adicione blocos de tempo no seu calendário (Acesse a aba Calendário para ver).")
        }
    }
}
