with open("app/src/main/java/com/example/presentation/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

member_since = """                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Membro desde: ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )"""

content = content.replace('color = MaterialTheme.colorScheme.secondary\n                )', 'color = MaterialTheme.colorScheme.secondary\n                )' + member_since)

with open("app/src/main/java/com/example/presentation/screens/ProfileScreen.kt", "w") as f:
    f.write(content)
