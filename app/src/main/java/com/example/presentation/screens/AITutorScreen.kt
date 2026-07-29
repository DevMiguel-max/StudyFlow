package com.example.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.local.AIConversation
import com.example.data.local.AIMessage
import com.example.presentation.viewmodel.AITutorViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AITutorScreen(
    navController: NavController,
    viewModel: AITutorViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StudyFlow AI") },
                navigationIcon = {
                    if (state.currentConversationId != null) {
                        IconButton(onClick = { viewModel.selectConversation(-1L); viewModel.selectConversation(0L); /* hack to deselect */ }) {
                            Icon(Icons.Default.ArrowBack, "Voltar")
                        }
                    } else {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Voltar")
                        }
                    }
                },
                actions = {
                    if (state.currentConversationId != null) {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, "Configurações")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.currentConversationId == null || state.currentConversationId == 0L || state.currentConversationId == -1L) {
                ConversationList(
                    conversations = state.conversations,
                    onSelect = { viewModel.selectConversation(it) },
                    onCreateNew = { viewModel.createNewConversation() },
                    onDelete = { viewModel.deleteConversation(it) }
                )
            } else {
                ChatInterface(
                    state = state,
                    onSendMessage = { viewModel.sendMessage(it) },
                    onClearHistory = { viewModel.clearHistory() }
                )
            }
        }
    }

    if (showSettings) {
        var temp by remember { mutableFloatStateOf(state.temperature) }
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Configurações do Tutor") },
            text = {
                Column {
                    Text("Temperatura: ${temp}")
                    Slider(
                        value = temp,
                        onValueChange = { temp = it },
                        valueRange = 0f..2f
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.clearHistory(); showSettings = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Limpar Histórico Desta Conversa")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.updateSettings(temp, state.maxTokens, state.selectedModel)
                    showSettings = false 
                }) { Text("Salvar") }
            }
        )
    }
}

@Composable
fun ConversationList(
    conversations: List<AIConversation>,
    onSelect: (Long) -> Unit,
    onCreateNew: () -> Unit,
    onDelete: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onCreateNew,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nova Conversa")
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(conversations) { conv ->
                ListItem(
                    headlineContent = { Text(conv.title, maxLines = 1) },
                    supportingContent = { 
                        Text(SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(conv.updatedAt)))
                    },
                    trailingContent = {
                        IconButton(onClick = { onDelete(conv.id) }) {
                            Icon(Icons.Default.Delete, "Excluir")
                        }
                    },
                    modifier = Modifier.clickable { onSelect(conv.id) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ChatInterface(
    state: com.example.presentation.viewmodel.AITutorState,
    onSendMessage: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    Column(modifier = Modifier.fillMaxSize()) {
        if (state.error != null) {
            Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                Text(state.error!!, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(8.dp))
            }
        }
        
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
            reverseLayout = true
        ) {
            if (state.isGenerating) {
                item {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
                }
            }
            items(state.messages.reversed()) { msg ->
                MessageBubble(msg, context)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    Toast.makeText(context, "Anexos (PDF, Imagem, DOCX, TXT) estarão disponíveis na próxima fase.", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.AttachFile, "Anexar")
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Pergunte ao tutor...") },
                    maxLines = 5,
                    shape = RoundedCornerShape(24.dp),
                    supportingText = {
                        Text("${inputText.length} caracteres", modifier = Modifier.fillMaxWidth())
                    }
                )
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !state.isGenerating
                ) {
                    Icon(Icons.Default.Send, "Enviar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: AIMessage, context: Context) {
    val isUser = message.isUser
    val bgColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 0.dp,
                    bottomEnd = if (isUser) 0.dp else 16.dp
                ))
                .background(bgColor)
                .padding(16.dp)
        ) {
            Column {
                if (isUser) {
                    Text(
                        text = message.text,
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    MarkdownText(
                        markdown = message.text,
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    color = textColor.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                )
            }
        }
        
        if (!isUser) {
            Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.Start) {
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Mensagem IA", message.text))
                    Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ContentCopy, "Copiar", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, message.text)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, "Compartilhar", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = {
                    Toast.makeText(context, "Regenerar estará disponível em breve.", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, "Regenerar", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = {
                    Toast.makeText(context, "Favoritado!", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.StarBorder, "Favoritar", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
