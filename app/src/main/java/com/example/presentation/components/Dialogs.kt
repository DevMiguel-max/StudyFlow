package com.example.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.Subject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteSubjectDialog(
    subject: Subject,
    availableSubjects: List<Subject>,
    onDismiss: () -> Unit,
    onConfirmDeleteAll: () -> Unit,
    onConfirmMoveTasks: (newSubjectId: Int) -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }
    
    val otherSubjects = availableSubjects.filter { it.id != subject.id }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir ${subject.name}?") },
        text = {
            Column {
                Text("O que deseja fazer com as tarefas associadas a esta matéria?")
                Spacer(modifier = Modifier.height(16.dp))
                
                if (otherSubjects.isNotEmpty()) {
                    Text("Mover para outra matéria:", style = MaterialTheme.typography.labelMedium)
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = otherSubjects.find { it.id == selectedSubjectId }?.name ?: "Selecione",
                            onValueChange = { },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            otherSubjects.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        selectedSubjectId = s.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text("Você não possui outras matérias. Todas as tarefas serão excluídas.", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            if (otherSubjects.isNotEmpty() && selectedSubjectId != null) {
                TextButton(onClick = { onConfirmMoveTasks(selectedSubjectId!!) }) {
                    Text("Mover Tarefas e Excluir")
                }
            } else {
                TextButton(onClick = onConfirmDeleteAll, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Excluir Tudo")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
