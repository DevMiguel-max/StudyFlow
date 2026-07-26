import re

with open("app/src/main/java/com/example/presentation/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

imports = """import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.clickable
import com.example.presentation.viewmodel.AuthViewModel
"""
content = content.replace("import com.example.presentation.viewmodel.ProfileViewModel", imports + "import com.example.presentation.viewmodel.ProfileViewModel")

viewmodels = """fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {"""
content = content.replace("""fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {""", viewmodels)

state_vars = """    val state by viewModel.state.collectAsState()
    val profile = state.userProfile
    val currentUser by authViewModel.currentUser.collectAsState()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editGoal by remember { mutableStateOf("") }
    var editIcon by remember { mutableStateOf("") }"""
content = content.replace("""    val state by viewModel.state.collectAsState()
    val profile = state.userProfile""", state_vars)

dialog_ui = """            return@Scaffold
        }
        
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Editar Perfil") },
                text = {
                    Column {
                        OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Nome") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = editGoal, onValueChange = { editGoal = it }, label = { Text("Objetivo Principal") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = editIcon, onValueChange = { editIcon = it }, label = { Text("Ícone (Emoji)") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateProfileInfo(editName, editGoal, editIcon)
                        showEditDialog = false
                    }) { Text("Salvar") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
                }
            )
        }"""
content = content.replace("return@Scaffold\n        }", dialog_ui)

name_ui = """                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = { 
                        editName = profile.name
                        editGoal = profile.mainGoal
                        editIcon = profile.avatarIcon
                        showEditDialog = true 
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                    }
                }
                Text(
                    text = currentUser?.email ?: profile.email.ifEmpty { "Usuário Offline" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Objetivo: ${profile.mainGoal}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nível ${profile.level}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )"""
content = re.sub(r'Spacer\(modifier = Modifier.height\(16.dp\)\)\s*Text\(\s*text = "Estudante",[\s\S]*?color = MaterialTheme.colorScheme.primary\s*\)', name_ui, content)

with open("app/src/main/java/com/example/presentation/screens/ProfileScreen.kt", "w") as f:
    f.write(content)
