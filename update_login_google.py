import re

with open("app/src/main/java/com/example/presentation/screens/AuthScreens.kt", "r") as f:
    content = f.read()

google_btn = """                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { /* Handle Google Sign In via CredentialManager here */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Entrar com Google")
                }"""

content = content.replace('Text("Entrar")\n                }', 'Text("Entrar")\n                }' + google_btn)

with open("app/src/main/java/com/example/presentation/screens/AuthScreens.kt", "w") as f:
    f.write(content)
