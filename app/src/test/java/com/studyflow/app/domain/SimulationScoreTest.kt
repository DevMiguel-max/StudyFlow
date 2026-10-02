package com.studyflow.app.domain

import com.studyflow.app.domain.manager.GamificationManager
import com.studyflow.app.presentation.viewmodel.SimulationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulationScoreTest {

    private lateinit var fakeRepository: FakeStudyFlowRepository
    private lateinit var gamificationManager: GamificationManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeStudyFlowRepository()
        gamificationManager = GamificationManager(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun simulationScoreCalculation_isAccurateForEnemScale() {
        val totalQuestions = 90
        val correctAnswers = 72
        val wrongAnswers = totalQuestions - correctAnswers

        val score = (correctAnswers.toFloat() / totalQuestions) * 1000f

        assertEquals(18, wrongAnswers)
        assertEquals(800.0f, score, 0.001f)
    }

    @Test
    fun simulationViewModel_calculatesScoreAndWrongAnswersCorrectly() = runTest(testDispatcher) {
        val viewModel = SimulationViewModel(fakeRepository, gamificationManager)

        viewModel.addSimulation(
            name = "Simulado ENEM 2026 - 1º Dia",
            examType = "ENEM",
            totalQuestions = 90,
            correctAnswers = 63
        )
        advanceUntilIdle()

        assertTrue(fakeRepository.insertedSimulations.isNotEmpty())
        val inserted = fakeRepository.insertedSimulations.first()

        assertEquals("Simulado ENEM 2026 - 1º Dia", inserted.name)
        assertEquals(90, inserted.totalQuestions)
        assertEquals(63, inserted.correctAnswers)
        assertEquals(27, inserted.wrongAnswers)
        // 63 / 90 * 1000 = 700.0
        assertEquals(700.0f, inserted.score, 0.001f)
    }
}
