package com.studyflow.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AIConsentDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Consentimento para Recursos de IA",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Este recurso utiliza inteligência artificial fornecida pelo Google Gemini (Google LLC). Antes de prosseguir, leia atentamente:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "• Processamento Internacional por Terceiro:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "O conteúdo digitado, perguntas ou documentos enviados serão transmitidos e processados nos servidores do Google nos Estados Unidos e outras regiões (transferência internacional de dados).",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "• Não envie Dados Sensíveis:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "Não digite nem compartilhe dados pessoais sensíveis (ex: CPF, dados de saúde, biometria, informações financeiras) ou dados pessoais de terceiros sem autorização.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text(
                    text = "Você pode alterar ou revogar esta permissão a qualquer momento na tela de Configurações do aplicativo. Se revogado, o uso dos recursos de IA voltará a solicitar consentimento prévio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onAccept) {
                Text("Aceitar e continuar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Agora não")
            }
        }
    )
}
