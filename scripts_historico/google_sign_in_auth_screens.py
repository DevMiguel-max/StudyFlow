with open("app/src/main/java/com/example/presentation/screens/AuthScreens.kt", "r") as f:
    content = f.read()

imports = """import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
"""

content = content.replace("import com.example.presentation.viewmodel.AuthViewModel", imports + "import com.example.presentation.viewmodel.AuthViewModel")

vars_setup = """    val currentUser by viewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()"""

content = content.replace("    val currentUser by viewModel.currentUser.collectAsState()", vars_setup)

btn = """                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val credentialManager = CredentialManager.create(context)
                                
                                // O Web Client ID geralmente fica no BuildConfig ou secrets.
                                // Usando placeholder para exemplo. Você deve usar a chave de cliente real do GCP.
                                // Isso normalmente vem de uma R.string.default_web_client_id que o google-services gera
                                val webClientId = "123456789-dummy.apps.googleusercontent.com" // Substituir pelo correto
                                
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
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Entrar com Google")
                }"""

content = content.replace("""                OutlinedButton(
                    onClick = { /* Handle Google Sign In via CredentialManager here */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Entrar com Google")
                }""", btn)

with open("app/src/main/java/com/example/presentation/screens/AuthScreens.kt", "w") as f:
    f.write(content)
