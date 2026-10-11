package com.studyflow.app.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.studyflow.app.presentation.viewmodel.FlashcardReviewViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardReviewScreen(
    navController: NavController,
    subjectId: Int? = null,
    viewModel: FlashcardReviewViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(subjectId) {
        viewModel.startSession(subjectId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisão Leitner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar para a tela anterior"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.testTag("loading_indicator"))
                }

                state.cardsQueue.isEmpty() -> {
                    EmptyFlashcardsView(onBack = { navController.popBackStack() })
                }

                state.isSessionCompleted -> {
                    SessionCompletedView(
                        total = state.totalCards,
                        correct = state.correctCount,
                        incorrect = state.incorrectCount,
                        earnedXp = state.earnedXp,
                        onFinish = { navController.popBackStack() },
                        onRestart = { viewModel.restartSession() }
                    )
                }

                state.currentCard != null -> {
                    val card = state.currentCard!!
                    ActiveCardView(
                        currentCardNumber = state.currentIndex + 1,
                        totalCards = state.totalCards,
                        box = card.box,
                        question = card.question,
                        answer = card.answer,
                        isAnswerVisible = state.isAnswerVisible,
                        onShowAnswer = { viewModel.showAnswer() },
                        onAnswer = { correct -> viewModel.answerCard(correct) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFlashcardsView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .testTag("empty_flashcards_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nenhum cartão para revisar hoje",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Todos os seus flashcards estão em dia de acordo com o intervalo do Sistema Leitner.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .testTag("empty_back_button")
        ) {
            Text("Voltar ao Início")
        }
    }
}

@Composable
private fun ActiveCardView(
    currentCardNumber: Int,
    totalCards: Int,
    box: Int,
    question: String,
    answer: String,
    isAnswerVisible: Boolean,
    onShowAnswer: () -> Unit,
    onAnswer: (Boolean) -> Unit
) {
    val intervalText = when (box) {
        1 -> "Caixa 1 • 1 dia"
        2 -> "Caixa 2 • 3 dias"
        else -> "Caixa 3 • 7 dias"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra de progresso e contadores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$currentCardNumber de $totalCards",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            AssistChip(
                onClick = {},
                label = { Text(intervalText) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { currentCardNumber.toFloat() / totalCards },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .testTag("review_progress_bar"),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Cartão principal de pergunta/resposta
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .clickable { if (!isAnswerVisible) onShowAnswer() }
                .testTag("flashcard_item"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "PERGUNTA",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                AnimatedVisibility(
                    visible = isAnswerVisible,
                    enter = fadeIn() + expandVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "RESPOSTA",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Controles de ação
        if (!isAnswerVisible) {
            Button(
                onClick = onShowAnswer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("show_answer_button")
            ) {
                Text("Mostrar resposta", fontSize = 16.sp)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onAnswer(false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("wrong_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Marcar que errou o cartão e voltar para a caixa 1"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Errei")
                }

                Button(
                    onClick = { onAnswer(true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("correct_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Marcar que acertou o cartão e avançar para a próxima caixa"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Acertei")
                }
            }
        }
    }
}

@Composable
private fun SessionCompletedView(
    total: Int,
    correct: Int,
    incorrect: Int,
    earnedXp: Int,
    onFinish: () -> Unit,
    onRestart: () -> Unit
) {
    val accuracy = if (total > 0) (correct.toFloat() / total * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("session_completed_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏆", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Sessão Concluída!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Você revisou todos os cartões programados para hoje.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Resumo da Revisão", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total revisado:")
                    Text("$total cartões", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Acertos:")
                    Text("$correct ($accuracy%)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Erros:")
                    Text("$incorrect", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
                if (earnedXp > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("XP Conquistado:")
                        Text("+$earnedXp XP", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Próxima revisão: Cartões acertados avançaram para a próxima caixa (3 ou 7 dias). Cartões com erro retornaram para a Caixa 1 (1 dia).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier
                    .weight(1f)
                    .testTag("restart_session_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reiniciar sessão de revisão")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Revisar de Novo")
            }

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .weight(1f)
                    .testTag("finish_session_button")
            ) {
                Text("Concluir")
            }
        }
    }
}
