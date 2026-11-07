package com.studyflow.app.presentation.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.studyflow.app.R
import com.studyflow.app.domain.manager.AgeGroup
import com.studyflow.app.domain.manager.PrivacyManager
import com.studyflow.app.domain.repository.SyncStatus
import com.studyflow.app.domain.util.UserDataExporter
import com.studyflow.app.presentation.viewmodel.AuthViewModel
import com.studyflow.app.presentation.viewmodel.ProfileViewModel
import com.studyflow.app.presentation.viewmodel.SyncViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel(),
    syncViewModel: SyncViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    privacyManager: PrivacyManager = koinInject(),
    userDataExporter: UserDataExporter = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val profile = state.userProfile
    
    val syncStatus by syncViewModel.syncStatus.collectAsState()
    val lastSyncTime by syncViewModel.lastSyncTime.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.state.collectAsState()
    
    val analyticsOptIn by privacyManager.analyticsOptIn.collectAsState(initial = false)
    val ageGroupEnum by privacyManager.ageGroupEnum.collectAsState(initial = AgeGroup.ADULT_18_PLUS)
    val aiConsentGranted by privacyManager.aiConsentGranted.collectAsState(initial = false)
    val termsAcceptedVersion by privacyManager.termsAcceptedVersion.collectAsState(initial = null)
    val termsAcceptedTimestamp by privacyManager.termsAcceptedTimestamp.collectAsState(initial = 0L)
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var gamificationEnabled by remember(profile) { mutableStateOf(profile?.gamificationEnabled ?: true) }
    var soundsEnabled by remember(profile) { mutableStateOf(profile?.soundsEnabled ?: true) }
    var notificationsEnabled by remember(profile) { mutableStateOf(profile?.notificationsEnabled ?: true) }
    var autoSyncEnabled by remember { mutableStateOf(true) }
    var wifiOnlyEnabled by remember { mutableStateOf(true) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showRevokeTermsDialog by remember { mutableStateOf(false) }
    var isExportingData by remember { mutableStateOf(false) }
    var reauthPassword by remember { mutableStateOf("") }
    var reauthPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(authState.isAccountDeleted) {
        if (authState.isAccountDeleted) {
            Toast.makeText(context, "Conta e dados excluídos com sucesso.", Toast.LENGTH_LONG).show()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

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
            // Seção 1: Preferências Locais
            Text("Preferências Locais", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
            
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
            
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Seção 2: Privacidade e Proteção de Dados (LGPD & Marco Civil & ECA Digital)
            Text("Privacidade e Proteção de Dados", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Exportar meus dados
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Exportar meus dados", fontWeight = FontWeight.Bold)
                            Text(
                                "Gera arquivo JSON completo com seus dados (LGPD Art. 18, V)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    isExportingData = true
                                    val result = userDataExporter.exportUserDataToJson(context)
                                    isExportingData = false
                                    result.onSuccess { uri ->
                                        UserDataExporter.shareExportedFile(context, uri)
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Erro ao exportar dados: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = !isExportingData
                        ) {
                            if (isExportingData) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Exportar")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Telemetria / Analytics opt-in
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Compartilhar dados de uso (opcional)", fontWeight = FontWeight.Bold)
                            val analyticsNotice = if (ageGroupEnum == AgeGroup.TEEN_12_17) {
                                "Desativado permanentemente para adolescentes (12 a 17 anos) conforme o ECA Digital."
                            } else {
                                "Envio de métricas anônimas para melhoria contínua do aplicativo."
                            }
                            Text(
                                text = analyticsNotice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = if (ageGroupEnum == AgeGroup.TEEN_12_17) false else analyticsOptIn,
                            onCheckedChange = { optIn ->
                                scope.launch { privacyManager.setAnalyticsEnabled(optIn) }
                            },
                            enabled = ageGroupEnum != AgeGroup.TEEN_12_17
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Consentimento para IA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Consentimento para Recursos de IA", fontWeight = FontWeight.Bold)
                            Text(
                                text = if (aiConsentGranted) {
                                    "Autorizado: conteúdo pode ser enviado para processamento no Google Gemini. Desative para revogar."
                                } else {
                                    "Não autorizado: será exibido um diálogo de consentimento antes de utilizar os recursos de IA."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = aiConsentGranted,
                            onCheckedChange = { granted ->
                                scope.launch { privacyManager.setAiConsent(granted) }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Faixa Etária e Termos Declarados
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Faixa Etária Declarada (ECA Digital)", fontWeight = FontWeight.Bold)
                        Text(
                            text = ageGroupEnum.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val formattedDate = if (termsAcceptedTimestamp > 0L) {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(termsAcceptedTimestamp))
                        } else {
                            "Não registrado"
                        }
                        Text(
                            text = "Termos aceitos: versão ${termsAcceptedVersion ?: "1.0"} em $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showRevokeTermsDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Revogar Termos e Redefinir Consentimentos")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Links jurídicos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { navController.navigate("terms_of_service") }) {
                            Text("Termos de Uso")
                        }
                        TextButton(onClick = { navController.navigate("privacy_policy") }) {
                            Text("Política de Privacidade")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Seção 3: Diagnóstico de IA
            Text("Diagnóstico de IA", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            AISettingsCard()

            Spacer(modifier = Modifier.height(28.dp))
            
            // Seção 4: Nuvem e Conta
            Text("Nuvem e Sincronização", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (currentUser != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sincronização Automática", fontWeight = FontWeight.Bold)
                        Text("Sincronizar em segundo plano", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoSyncEnabled,
                        onCheckedChange = { autoSyncEnabled = it }
                    )
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                
                OutlinedButton(
                    onClick = { 
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sair da Conta")
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { showDeleteConfirmDialog = true },
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

    // Diálogo 1: Confirmação de Exclusão de Conta (Art. 18, VI da LGPD)
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Excluir Conta Definitivamente", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Atenção: esta ação é IRREVERSÍVEL.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ao confirmar, todo o seu histórico e dados serão excluídos permanentemente:\n" +
                                "• Todas as matérias, tarefas, metas e sessões de estudo;\n" +
                                "• Flashcards, anotações, mapas mentais e simulados;\n" +
                                "• Redações, materiais gerados e diagnósticos de IA;\n" +
                                "• Documentos armazenados na nuvem (Firestore);\n" +
                                "• Todas as tabelas do banco de dados local no aparelho;\n" +
                                "• Sua conta de acesso no servidor de autenticação.\n\n" +
                                "Deseja realmente prosseguir com a exclusão definitiva?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        authViewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sim, excluir minha conta")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo 2: Reautenticação necessária (FirebaseAuthRecentLoginRequiredException)
    if (authState.isRecentLoginRequired) {
        AlertDialog(
            onDismissRequest = { authViewModel.dismissRecentLoginRequired() },
            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Confirmação de Segurança", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Por questões de segurança exigidas pelo Firebase, confirme suas credenciais para autorizar a exclusão definitiva da conta.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = reauthPassword,
                        onValueChange = { reauthPassword = it },
                        label = { Text("Senha atual") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            val icon = if (reauthPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { reauthPasswordVisible = !reauthPasswordVisible }) {
                                Icon(icon, contentDescription = null)
                            }
                        },
                        visualTransformation = if (reauthPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            authViewModel.reauthenticateAndRetryDelete(reauthPassword)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = reauthPassword.isNotEmpty() && !authState.isLoading
                    ) {
                        Text("Confirmar com Senha e Excluir")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "ou",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            launchGoogleReauth(
                                context = context,
                                onSuccess = { idToken ->
                                    authViewModel.reauthenticateWithGoogleAndRetryDelete(idToken)
                                },
                                onError = { errorMsg ->
                                    authViewModel.setError(errorMsg)
                                },
                                scope = scope
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !authState.isLoading
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = "Google",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reautenticar com Google")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { authViewModel.dismissRecentLoginRequired() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo 3: Erro durante a exclusão ou reautenticação
    if (authState.error != null) {
        AlertDialog(
            onDismissRequest = { authViewModel.clearError() },
            icon = { Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Aviso de Exclusão") },
            text = { Text(authState.error!!) },
            confirmButton = {
                Button(onClick = { authViewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    // Diálogo 4: Loading durante a exclusão
    if (authState.isLoading) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Excluindo conta e dados...") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Removendo dados do Firestore e limpando banco de dados local com segurança...")
                }
            },
            confirmButton = {}
        )
    }

    // Diálogo 5: Confirmação de Revogação de Termos
    if (showRevokeTermsDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeTermsDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Revogar Termos e Consentimentos", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Ao revogar os termos e a política de privacidade, seus consentimentos e verificação de faixa etária serão redefinidos. Você será redirecionado para a tela inicial de validação para configurar novamente seus consentimentos para continuar usando o app.\n\nDeseja continuar?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRevokeTermsDialog = false
                        scope.launch {
                            privacyManager.revokeTermsConsent()
                            navController.navigate("welcome_consent") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text("Revogar e Redefinir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeTermsDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun launchGoogleReauth(
    context: Context,
    onSuccess: (idToken: String) -> Unit,
    onError: (String) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val activity = context.findActivity()
    if (activity == null) {
        onError("Janela do app indisponível para reautenticar com o Google.")
        return
    }
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onError("Configuração do Google Sign-In ausente.")
        return
    }
    val credentialManager = CredentialManager.create(context)
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                onSuccess(googleIdToken)
            } else {
                onError("Tipo de credencial incompatível.")
            }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Falha ao reautenticar com o Google.")
        }
    }
}

@Composable
fun AISettingsCard() {
    val telemetryStats by com.studyflow.app.domain.ai.AITelemetry.stats.collectAsState()
    val context = LocalContext.current
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
                            Toast.makeText(context, "Exportado para cache!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Exportar diagnóstico de IA")
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
