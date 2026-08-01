package com.studyflow.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.studyflow.app.domain.repository.SyncStatus
import com.studyflow.app.presentation.viewmodel.AuthViewModel
import com.studyflow.app.presentation.viewmodel.ProfileViewModel
import com.studyflow.app.presentation.viewmodel.SyncViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel(),
    syncViewModel: SyncViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val profile = state.userProfile
    
    val syncStatus by syncViewModel.syncStatus.collectAsState()
    val lastSyncTime by syncViewModel.lastSyncTime.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    var gamificationEnabled by remember(profile) { mutableStateOf(profile?.gamificationEnabled ?: true) }
    var soundsEnabled by remember(profile) { mutableStateOf(profile?.soundsEnabled ?: true) }
    var notificationsEnabled by remember(profile) { mutableStateOf(profile?.notificationsEnabled ?: true) }
    var autoSyncEnabled by remember { mutableStateOf(true) }
    var wifiOnlyEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Preferências Locais", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Gamificação", fontWeight = FontWeight.Bold)
                    Text("XP, Níveis e Conquistas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = gamificationEnabled,
                    onCheckedChange = { 
                        gamificationEnabled = it
                        viewModel.updateSettings(gamificationEnabled, soundsEnabled, notificationsEnabled)
                    }
                )
            }
            
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sons do App", fontWeight = FontWeight.Bold)
                    Text("Efeitos ao completar ações", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = soundsEnabled,
                    onCheckedChange = { 
                        soundsEnabled = it
                        viewModel.updateSettings(gamificationEnabled, soundsEnabled, notificationsEnabled)
                    }
                )
            }
            
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Notificações Locais", fontWeight = FontWeight.Bold)
                    Text("Lembretes de estudo e conquistas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { 
                        notificationsEnabled = it
                        viewModel.updateSettings(gamificationEnabled, soundsEnabled, notificationsEnabled)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Nuvem e Sincronização", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (currentUser != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Sincronização Automática", fontWeight = FontWeight.Bold)
                        Text("Sincronizar em segundo plano", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoSyncEnabled,
                        onCheckedChange = { autoSyncEnabled = it }
                    )
                }
                
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Apenas no Wi-Fi", fontWeight = FontWeight.Bold)
                        Text("Economizar dados móveis", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = wifiOnlyEnabled,
                        onCheckedChange = { wifiOnlyEnabled = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Status da Nuvem", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val statusText = when (syncStatus) {
                            SyncStatus.IDLE -> "Ocioso"
                            SyncStatus.SYNCING -> "Sincronizando..."
                            SyncStatus.SUCCESS -> "Sincronizado"
                            SyncStatus.ERROR -> "Erro na sincronização"
                        }
                        Text("Status: $statusText", style = MaterialTheme.typography.bodyMedium)
                        
                        val timeStr = if (lastSyncTime > 0) {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(lastSyncTime))
                        } else {
                            "Nunca"
                        }
                        Text("Última sincronização: $timeStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { syncViewModel.syncNow() }, enabled = syncStatus != SyncStatus.SYNCING) {
                                Text("Sincronizar Agora")
                            }
                            OutlinedButton(onClick = { syncViewModel.restoreBackup() }, enabled = syncStatus != SyncStatus.SYNCING) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restaurar")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { 
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sair da Conta")
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        // In a real app we would prompt for confirmation
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir Conta")
                }

            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Você está offline", fontWeight = FontWeight.Bold)
                        Text("Faça login para salvar seus dados na nuvem.", style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { 
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        }) {
                            Text("Fazer Login")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AISettingsCard() {
    val telemetryStats by com.studyflow.app.domain.ai.AITelemetry.stats.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("StudyFlow AI", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Requisições Totais: ${telemetryStats.totalRequests}", style = MaterialTheme.typography.bodyMedium)
            Text("Completadas: ${telemetryStats.successfulRequests} | Falhas: ${telemetryStats.failedRequests}", style = MaterialTheme.typography.bodyMedium)
            Text("Latência Média: ${telemetryStats.averageLatencyMs} ms", style = MaterialTheme.typography.bodyMedium)
            Text("Tokens (Estimado): ~${telemetryStats.estimatedTokensUsed}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { scope.launch { com.studyflow.app.domain.ai.AICacheManager.clear() } },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpar Cache")
                }
                OutlinedButton(
                    onClick = {
                        val report = """
                            Total Requests: ${telemetryStats.totalRequests}
                            Success: ${telemetryStats.successfulRequests}
                            Failed: ${telemetryStats.failedRequests}
                            Avg Latency: ${telemetryStats.averageLatencyMs} ms
                            Est Tokens: ${telemetryStats.estimatedTokensUsed}
                        """.trimIndent()
                        val uri = com.studyflow.app.domain.util.ExportManager.exportToTxt(context, "AI_Telemetry", report)
                        if (uri != null) {
                            android.widget.Toast.makeText(context, "Exportado para cache!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Exportar")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { com.studyflow.app.domain.ai.AITelemetry.clearStats() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Resetar Estatísticas")
            }
        }
    }
}
