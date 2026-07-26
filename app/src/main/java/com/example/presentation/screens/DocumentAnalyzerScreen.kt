package com.example.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.local.AnalyzedDocument
import com.example.presentation.viewmodel.DocumentAnalyzerViewModel
import org.koin.androidx.compose.koinViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentAnalyzerScreen(
    navController: NavController,
    viewModel: DocumentAnalyzerViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importAndAnalyzeDocument(context, it)
        }
    }

    if (state.currentDocument != null) {
        DocumentDetailScreen(
            document = state.currentDocument!!,
            onBack = { viewModel.selectDocument(null) },
            onDelete = { 
                viewModel.deleteDocument(state.currentDocument!!)
            },
            onGenerateSummary = { type ->
                viewModel.generateSummary(state.currentDocument!!.id, type)
            },
            isProcessing = state.isProcessing,
            progressMessage = state.progressMessage
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Analisador de Documentos") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    filePickerLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/plain", "text/markdown"))
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Importar Documento")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search and Filters
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquisar documentos...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("Todos", "EDITAL", "LIVRO", "OUTROS")
                    filters.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }

                if (state.isProcessing) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.progressMessage, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // List
                val filteredDocs = documents.filter {
                    (selectedFilter == "Todos" || it.documentType == selectedFilter) &&
                    it.title.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredDocs) { doc ->
                        DocumentCard(doc, onClick = { viewModel.selectDocument(doc) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentCard(doc: AnalyzedDocument, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(doc.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(doc.documentType, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("Analisado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    document: AnalyzedDocument,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onGenerateSummary: (String) -> Unit,
    isProcessing: Boolean,
    progressMessage: String
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Análise", "Resumo", "Plano")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(document.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (isProcessing) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(progressMessage, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        when (selectedTab) {
                            0 -> {
                                Text("Análise da IA", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                MarkdownText(markdown = document.analysisResult ?: "Nenhuma análise disponível.")
                            }
                            1 -> {
                                Text("Opções de Resumo", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(onClick = { onGenerateSummary("SHORT") }, modifier = Modifier.weight(1f)) {
                                        Text("Curto")
                                    }
                                    Button(onClick = { onGenerateSummary("MEDIUM") }, modifier = Modifier.weight(1f)) {
                                        Text("Médio")
                                    }
                                    Button(onClick = { onGenerateSummary("COMPLETE") }, modifier = Modifier.weight(1f)) {
                                        Text("Completo")
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                if (document.summaryShort != null) {
                                    Text("Resumo Curto:", style = MaterialTheme.typography.titleMedium)
                                    MarkdownText(markdown = document.summaryShort)
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                if (document.summaryMedium != null) {
                                    Text("Resumo Médio:", style = MaterialTheme.typography.titleMedium)
                                    MarkdownText(markdown = document.summaryMedium)
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                if (document.summaryComplete != null) {
                                    Text("Resumo Completo:", style = MaterialTheme.typography.titleMedium)
                                    MarkdownText(markdown = document.summaryComplete)
                                }
                            }
                            2 -> {
                                Text("Plano de Estudos / Integração", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (document.documentType == "EDITAL") {
                                    Text("O plano de estudos baseado no edital foi gerado e integrado ao módulo de Planejamento.", style = MaterialTheme.typography.bodyMedium)
                                    // In the future: Button to navigate to Plan
                                } else {
                                    Text("Geração automática de plano focada em editais.", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
