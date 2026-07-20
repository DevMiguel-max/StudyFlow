package com.example.presentation.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.presentation.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(navController: NavController, title: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) }
            )
        }
    ) { padding ->
        EmptyState(
            icon = "🚧",
            title = "Em Desenvolvimento",
            description = "Esta funcionalidade estará disponível em breve."
        )
    }
}
