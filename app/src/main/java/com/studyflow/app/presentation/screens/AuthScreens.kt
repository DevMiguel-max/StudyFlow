package com.studyflow.app.presentation.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.studyflow.app.R
import com.studyflow.app.domain.manager.AgeGroup
import com.studyflow.app.domain.manager.PrivacyManager
import com.studyflow.app.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

internal fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

internal fun launchGoogleSignIn(
    context: Context,
    viewModel: AuthViewModel,
    scope: CoroutineScope
) {
    val activity = context.findActivity()
    if (activity == null) {
        viewModel.setError("Não foi possível identificar a janela do app para o login Google.")
        return
    }

    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        viewModel.setError("Configuração Google Sign-In ausente: default_web_client_id não encontrado.")
        return
    }

    val credentialManager = CredentialManager.create(context)
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    viewModel.setLoading(true)
    scope.launch {
        try {
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                viewModel.signInWithGoogle(googleIdToken)
            } else {
                viewModel.setError("Tipo de credencial incompatível retornado pelo Google.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("AuthScreens", "Google Sign-In cancelado ou dispensado", e)
            viewModel.setLoading(false)
        } catch (e: Exception) {
            Log.e("AuthScreens", "Erro no fluxo de autenticação com o Google", e)
            viewModel.setError(e.localizedMessage ?: "Falha ao autenticar com o Google.")
        }
    }
}

@Composable
fun GoogleSignInButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google",
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = koinViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isEmailValid = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val emailError = if (email.isNotEmpty() && !isEmailValid) "E-mail inválido" else null

    val isPasswordValid = password.length >= 6
    val passwordError = if (password.isNotEmpty() && !isPasswordValid) "A senha deve ter no mínimo 6 caracteres" else null

    val isLoginFormValid = isEmailValid && isPasswordValid

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "StudyFlow",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Faça login para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // 1) Campo E-mail
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearError()
                },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2) Campo Senha com botão mostrar/ocultar
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearError()
                },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha") },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // 3) Botão "Entrar"
            Button(
                onClick = { viewModel.signInWithEmail(email.trim(), password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = isLoginFormValid && !state.isLoading
            ) {
                Text("Entrar", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = "ou",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            GoogleSignInButton(
                text = "Entrar com o Google",
                onClick = { launchGoogleSignIn(context, viewModel, scope) },
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 6) Links "Criar conta" e "Esqueci a senha"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { navController.navigate("signup") },
                    enabled = !state.isLoading
                ) {
                    Text("Criar conta")
                }
                TextButton(
                    onClick = { navController.navigate("recover_password") },
                    enabled = !state.isLoading
                ) {
                    Text("Esqueci a senha")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7) Botão "Continuar Offline"
            TextButton(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                enabled = !state.isLoading
            ) {
                Text("Continuar Offline", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { navController.navigate("terms_of_service") },
                    enabled = !state.isLoading
                ) {
                    Text("Termos de Uso", style = MaterialTheme.typography.bodySmall)
                }
                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
                TextButton(
                    onClick = { navController.navigate("privacy_policy") },
                    enabled = !state.isLoading
                ) {
                    Text("Política de Privacidade", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel = koinViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedAgeGroup by remember { mutableStateOf<AgeGroup?>(null) }
    var termsAccepted by remember { mutableStateOf(false) }

    val privacyManager: PrivacyManager = koinInject()
    val state by viewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isEmailValid = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val emailError = if (email.isNotEmpty() && !isEmailValid) "E-mail inválido" else null

    val isPasswordValid = password.length >= 6
    val passwordError = if (password.isNotEmpty() && !isPasswordValid) "A senha deve ter no mínimo 6 caracteres" else null

    val isConfirmValid = confirmPassword.isNotEmpty() && confirmPassword == password
    val confirmError = if (confirmPassword.isNotEmpty() && confirmPassword != password) "As senhas não coincidem" else null

    val isAgeAllowed = selectedAgeGroup != null && selectedAgeGroup != AgeGroup.UNDER_12
    val isSignUpFormValid = isEmailValid && isPasswordValid && isConfirmValid && termsAccepted && isAgeAllowed

    LaunchedEffect(currentUser, state.isSuccess) {
        if (currentUser != null) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        } else if (state.isSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text("Criar Conta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            // Campo E-mail
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearError()
                },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Senha
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearError()
                },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha") },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            // Indicador visual de força da senha
            PasswordStrengthIndicator(password = password)

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Confirmar senha
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    viewModel.clearError()
                },
                label = { Text("Confirmar Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirmar Senha") },
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (confirmPasswordVisible) "Ocultar senha" else "Mostrar senha"
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = confirmError != null,
                supportingText = confirmError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Faixa Etária (ECA Digital)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Faixa etária (ECA Digital — Lei 15.211/2025)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedAgeGroup == AgeGroup.UNDER_12,
                        onClick = { selectedAgeGroup = AgeGroup.UNDER_12 },
                        label = { Text("< 12 anos", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedAgeGroup == AgeGroup.TEEN_12_17,
                        onClick = { selectedAgeGroup = AgeGroup.TEEN_12_17 },
                        label = { Text("12 a 17 anos", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1.1f)
                    )
                    FilterChip(
                        selected = selectedAgeGroup == AgeGroup.ADULT_18_PLUS,
                        onClick = { selectedAgeGroup = AgeGroup.ADULT_18_PLUS },
                        label = { Text("18+ anos", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (selectedAgeGroup == AgeGroup.UNDER_12) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Cadastro Indisponível para menores de 12 anos",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Em conformidade com o ECA Digital (Lei 15.211/2025), o StudyFlow não permite o cadastro direto de crianças menores de 12 anos sem a representação formal e consentimento dos pais ou responsáveis legais.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else if (selectedAgeGroup == AgeGroup.TEEN_12_17) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Privacidade Protegida (12 a 17 anos)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "A coleta de estatísticas de uso e telemetria permanecerá permanentemente desativada para sua proteção e privacidade.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Checkbox não pré-marcado para Termos e Política
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    enabled = !state.isLoading
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Li e aceito os:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Termos de Uso",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { navController.navigate("terms_of_service") }
                        )
                        Text(
                            text = " e a ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Política de Privacidade",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { navController.navigate("privacy_policy") }
                        )
                    }
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // Botão "Cadastrar"
            Button(
                onClick = {
                    scope.launch {
                        privacyManager.recordTermsConsent()
                        selectedAgeGroup?.let { privacyManager.setAgeGroup(it) }
                    }
                    viewModel.signUpWithEmail(email.trim(), password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = isSignUpFormValid && !state.isLoading
            ) {
                Text("Cadastrar", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = "ou",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }

            Spacer(modifier = Modifier.height(16.dp))

            GoogleSignInButton(
                text = "Cadastrar com o Google",
                onClick = {
                    scope.launch {
                        privacyManager.recordTermsConsent()
                        selectedAgeGroup?.let { privacyManager.setAgeGroup(it) }
                    }
                    launchGoogleSignIn(context, viewModel, scope)
                },
                enabled = termsAccepted && isAgeAllowed && !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botão/link "Voltar"
            TextButton(
                onClick = { navController.popBackStack() },
                enabled = !state.isLoading
            ) {
                Text("Voltar")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = koinViewModel()
) {
    var email by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    val isEmailValid = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val emailError = if (email.isNotEmpty() && !isEmailValid) "E-mail inválido" else null

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Recuperar Senha", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Digite seu e-mail para receber o link de redefinição.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    viewModel.clearError()
                },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            if (state.isSuccess) {
                Text("E-mail enviado! Verifique sua caixa de entrada.", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.recoverPassword(email.trim()) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = isEmailValid && !state.isLoading
                ) {
                    Text("Enviar Link", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Voltar")
                }
            }
        }
    }
}

enum class PasswordStrength(
    val label: String,
    val level: Int,
    val color: Color
) {
    WEAK("Fraca", 1, Color(0xFFEF4444)),
    MEDIUM("Média", 2, Color(0xFFF59E0B)),
    STRONG("Forte", 3, Color(0xFF10B981));

    companion object {
        fun calculate(password: String): PasswordStrength {
            if (password.length < 6) return WEAK

            val hasMinLength = password.length >= 8
            val hasNumber = password.any { it.isDigit() }
            val hasSpecial = password.any { !it.isLetterOrDigit() }
            val hasUpperLower = password.any { it.isUpperCase() } && password.any { it.isLowerCase() }

            var score = 0
            if (hasMinLength) score++
            if (password.length >= 10) score++
            if (hasNumber) score++
            if (hasSpecial) score++
            if (hasUpperLower) score++

            return when {
                hasMinLength && hasNumber && hasSpecial && score >= 3 -> STRONG
                score >= 2 -> MEDIUM
                else -> WEAK
            }
        }
    }
}

@Composable
fun PasswordStrengthIndicator(password: String) {
    if (password.isEmpty()) return

    val strength = remember(password) { PasswordStrength.calculate(password) }
    val hasMinLength = password.length >= 8
    val hasNumber = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Força da senha",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = strength.label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = strength.color
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Barras de progresso com 3 segmentos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 1..3) {
                val isActive = strength.level >= i
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (isActive) strength.color
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Indicadores visuais de critérios adicionais
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PasswordCriteriaItem(
                label = "8+ caracteres",
                isMet = hasMinLength,
                modifier = Modifier.weight(1f)
            )
            PasswordCriteriaItem(
                label = "Número (0-9)",
                isMet = hasNumber,
                modifier = Modifier.weight(1f)
            )
            PasswordCriteriaItem(
                label = "Especial (!@#)",
                isMet = hasSpecial,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PasswordCriteriaItem(
    label: String,
    isMet: Boolean,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(0xFF10B981)
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isMet) activeColor else inactiveColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isMet) activeColor else inactiveColor
        )
    }
}

