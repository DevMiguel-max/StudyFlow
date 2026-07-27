with open("app/src/main/java/com/example/presentation/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

delete_account = """
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
"""

content = content.replace('Text("Sair da Conta")\n                }', 'Text("Sair da Conta")\n                }' + delete_account)

with open("app/src/main/java/com/example/presentation/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
