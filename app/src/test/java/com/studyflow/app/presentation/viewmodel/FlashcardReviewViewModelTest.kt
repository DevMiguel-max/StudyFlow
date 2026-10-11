package com.studyflow.app.presentation.viewmodel

import com.studyflow.app.data.local.Flashcard
import com.studyflow.app.data.local.UserProfile
import com.studyflow.app.domain.FakeStudyFlowRepository
import com.studyflow.app.domain.manager.GamificationManager
import com.studyflow.app.domain.util.LeitnerScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlashcardReviewViewModelTest {

    private lateinit var fakeRepository: FakeStudyFlowRepository
    private lateinit var gamificationManager: GamificationManager
    private lateinit var viewModel: FlashcardReviewViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testNow = 1_700_000_000_000L

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeStudyFlowRepository()
        gamificationManager = GamificationManager(fakeRepository)
        viewModel = FlashcardReviewViewModel(fakeRepository, gamificationManager)

        fakeRepository.userProfileState.value = UserProfile(
            xp = 100,
            level = 1,
            gamificationEnabled = true
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startSession_whenNoCardsDue_setsEmptyStateAndCompletedTrue() = runTest(testDispatcher) {
        viewModel.startSession(customNow = testNow)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(0, state.totalCards)
        assertTrue(state.isSessionCompleted)
        assertNull(state.currentCard)
        assertEquals(0, state.correctCount)
        assertEquals(0, state.incorrectCount)
    }

    @Test
    fun completeSession_withThreeCards_updatesBoxAndNextReviewDateCorrectly() = runTest(testDispatcher) {
        val card1 = Flashcard(
            id = 1,
            subjectId = 1,
            question = "Q1",
            answer = "A1",
            box = 1,
            nextReviewDate = testNow - 1000L
        )
        val card2 = Flashcard(
            id = 2,
            subjectId = 1,
            question = "Q2",
            answer = "A2",
            box = 2,
            nextReviewDate = testNow - 500L
        )
        val card3 = Flashcard(
            id = 3,
            subjectId = 1,
            question = "Q3",
            answer = "A3",
            box = 3,
            nextReviewDate = testNow
        )

        fakeRepository.flashcardsList.addAll(listOf(card1, card2, card3))

        viewModel.startSession(customNow = testNow)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(3, viewModel.state.value.totalCards)
        assertFalse(viewModel.state.value.isSessionCompleted)
        assertEquals(1, viewModel.state.value.currentCard?.id)

        // Responde Card 1 com ACERTO: Box 1 -> Box 2 (+3 dias)
        viewModel.showAnswer()
        assertTrue(viewModel.state.value.isAnswerVisible)
        viewModel.answerCard(correct = true, customNow = testNow)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.state.value.correctCount)
        assertEquals(0, viewModel.state.value.incorrectCount)
        assertEquals(2, viewModel.state.value.currentCard?.id)
        assertFalse(viewModel.state.value.isAnswerVisible)

        // Responde Card 2 com ERRO: Box 2 -> Box 1 (+1 dia)
        viewModel.answerCard(correct = false, customNow = testNow)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.state.value.correctCount)
        assertEquals(1, viewModel.state.value.incorrectCount)
        assertEquals(3, viewModel.state.value.currentCard?.id)

        // Responde Card 3 com ACERTO: Box 3 -> Box 3 (+7 dias)
        viewModel.answerCard(correct = true, customNow = testNow)
        testDispatcher.scheduler.advanceUntilIdle()

        // Sessão concluída
        val finalState = viewModel.state.value
        assertTrue(finalState.isSessionCompleted)
        assertEquals(2, finalState.correctCount)
        assertEquals(1, finalState.incorrectCount)
        assertEquals(15, finalState.earnedXp) // 3 cartões * 5 XP

        // Verifica persistência no repositório
        val persistedCard1 = fakeRepository.updatedFlashcards.find { it.id == 1 }
        assertNotNull(persistedCard1)
        assertEquals(2, persistedCard1!!.box)
        assertEquals(testNow + LeitnerScheduler.THREE_DAYS_MILLIS, persistedCard1.nextReviewDate)

        val persistedCard2 = fakeRepository.updatedFlashcards.find { it.id == 2 }
        assertNotNull(persistedCard2)
        assertEquals(1, persistedCard2!!.box)
        assertEquals(testNow + LeitnerScheduler.ONE_DAY_MILLIS, persistedCard2.nextReviewDate)

        val persistedCard3 = fakeRepository.updatedFlashcards.find { it.id == 3 }
        assertNotNull(persistedCard3)
        assertEquals(3, persistedCard3!!.box)
        assertEquals(testNow + LeitnerScheduler.SEVEN_DAYS_MILLIS, persistedCard3.nextReviewDate)
    }

    @Test
    fun showAnswer_togglesAnswerVisibilityOnlyWhenCardIsActive() = runTest(testDispatcher) {
        val card = Flashcard(
            id = 10,
            subjectId = 1,
            question = "Questão",
            answer = "Resposta",
            box = 1,
            nextReviewDate = testNow
        )
        fakeRepository.flashcardsList.add(card)

        viewModel.startSession(customNow = testNow)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isAnswerVisible)
        viewModel.showAnswer()
        assertTrue(viewModel.state.value.isAnswerVisible)
    }
}
