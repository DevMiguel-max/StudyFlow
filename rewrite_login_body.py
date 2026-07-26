import re

with open("app/src/main/java/com/example/presentation/screens/AuthScreens.kt", "r") as f:
    content = f.read()

# I will replace the Scaffold of LoginScreen with a new modern one.
# It starts at `Scaffold { padding ->` and ends before `@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun SignUpScreen(`

start_idx = content.find("    Scaffold { padding ->")
end_idx = content.find("@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun SignUpScreen")

new_scaffold = """    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "StudyFlow",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = "Faça login para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha") },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.signInWithEmail(email, password) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Entrar", style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val credentialManager = CredentialManager.create(context)
                                val webClientId = com.example.BuildConfig.WEB_CLIENT_ID.ifEmpty { "123456789-dummy.apps.googleusercontent.com" }
                                
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(webClientId)
                                    .setAutoSelectEnabled(true)
                                    .build()
                                    
                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()
                                    
                                val result = credentialManager.getCredential(
                                    request = request,
                                    context = context
                                )
                                
                                val credential = result.credential
                                if (credential is GoogleIdTokenCredential) {
                                    val idToken = credential.idToken
                                    viewModel.signInWithGoogle(idToken)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Entrar com Google", style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { navController.navigate("signup") }) {
                        Text("Criar conta")
                    }
                    TextButton(onClick = { navController.navigate("recover_password") }) {
                        Text("Esqueci a senha")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { 
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }) {
                    Text("Continuar Offline", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

"""

if start_idx != -1 and end_idx != -1:
    new_content = content[:start_idx] + new_scaffold + content[end_idx:]
    with open("app/src/main/java/com/example/presentation/screens/AuthScreens.kt", "w") as f:
        f.write(new_content)
else:
    print("Could not find boundaries")
