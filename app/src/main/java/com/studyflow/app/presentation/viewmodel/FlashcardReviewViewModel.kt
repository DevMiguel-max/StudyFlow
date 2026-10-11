package com.studyflow.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.local.Flashcard
import com.studyflow.app.domain.manager.GamificationManager
import com.studyflow.app.domain.repository.StudyFlowRepository
import com.studyflow.app.domain.util.LeitnerScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FlashcardReviewState(
    val isLoading: Boolean = true,
    val cardsQueue: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val isAnswerVisible: Boolean = false,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val isSessionCompleted: Boolean = false,
    val subjectId: Int? = null,
    val earnedXp: Int = 0
) {
    val totalCards: Int get() = cardsQueue.size
    val currentCard: Flashcard? get() = cardsQueue.getOrNull(currentIndex)
    val progress: Float get() = if (totalCards > 0) currentIndex.toFloat() / totalCards else 0f
}

class FlashcardReviewViewModel(
    private val repository: StudyFlowRepository,
    private val gamificationManager: GamificationManager
) : ViewModel() {

    private val _state = MutableStateFlow(FlashcardReviewState())
    val state: StateFlow<FlashcardReviewState> = _state.asStateFlow()

    fun startSession(subjectId: Int? = null, customNow: Long? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, subjectId = subjectId) }
            val now = customNow ?: System.currentTimeMillis()
            val dueCardsFlow = repository.getDueFlashcards(now)
            val allDueCards = dueCardsFlow.first()
            val filteredCards = if (subjectId != null) {
                allDueCards.filter { it.subjectId == subjectId }
            } else {
                allDueCards
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    cardsQueue = filteredCards,
                    currentIndex = 0,
                    isAnswerVisible = false,
                    correctCount = 0,
                    incorrectCount = 0,
                    isSessionCompleted = filteredCards.isEmpty(),
                    earnedXp = 0
                )
            }
        }
    }

    fun showAnswer() {
        if (!_state.value.isSessionCompleted && _state.value.currentCard != null) {
            _state.update { it.copy(isAnswerVisible = true) }
        }
    }

    fun answerCard(correct: Boolean, customNow: Long? = null) {
        val currentState = _state.value
        val card = currentState.currentCard ?: return
        val now = customNow ?: System.currentTimeMillis()

        viewModelScope.launch {
            val updatedCard = LeitnerScheduler.next(card, correct, now)
            repository.updateFlashcard(updatedCard)

            val newCorrect = if (correct) currentState.correctCount + 1 else currentState.correctCount
            val newIncorrect = if (!correct) currentState.incorrectCount + 1 else currentState.incorrectCount
            val nextIndex = currentState.currentIndex + 1
            val completed = nextIndex >= currentState.totalCards

            val earnedXp = if (completed) {
                val totalReviewed = currentState.totalCards
                gamificationManager.completeRevisionSession(totalReviewed)
                totalReviewed * 5
            } else 0

            _state.update {
                it.copy(
                    currentIndex = nextIndex,
                    isAnswerVisible = false,
                    correctCount = newCorrect,
                    incorrectCount = newIncorrect,
                    isSessionCompleted = completed,
                    earnedXp = if (completed) earnedXp else it.earnedXp
                )
            }
        }
    }

    fun restartSession(customNow: Long? = null) {
        startSession(_state.value.subjectId, customNow)
    }
}
