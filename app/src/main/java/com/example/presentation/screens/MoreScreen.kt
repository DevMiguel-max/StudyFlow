package com.example.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.presentation.components.AppTopBar

data class MoreMenuItem(val title: String, val icon: ImageVector, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController) {
    val items = listOf(
        MoreMenuItem("Estatísticas", Icons.Default.BarChart, "statistics"),
        MoreMenuItem("Redação", Icons.Default.Create, "essay_module"),
        MoreMenuItem("Métodos de Estudo", Icons.Default.Lightbulb, "study_methods"),
        MoreMenuItem("Preparação", Icons.Default.Flag, "preparation"),
        MoreMenuItem("Simulados", Icons.Default.Checklist, "simulations"),
        MoreMenuItem("Mentor IA", Icons.Default.AutoGraph, "mentor"),
        MoreMenuItem("AI Tutor", Icons.Default.SmartToy, "ai_tutor"),
        MoreMenuItem("Gerador de Material", Icons.Default.AutoFixHigh, "material_generator"),
        MoreMenuItem("Editais", Icons.Default.Description, "editais"),
        MoreMenuItem("Configurações", Icons.Default.Settings, "settings")
    )

    Scaffold { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            AppTopBar(title = "Mais Opções")
            
            Text(
                text = "Explorar",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable { navController.navigate(item.route) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
