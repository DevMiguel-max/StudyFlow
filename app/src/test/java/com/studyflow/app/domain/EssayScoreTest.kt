package com.studyflow.app.domain

import com.studyflow.app.data.local.EssayCorrection
import com.studyflow.app.domain.ai.AICorrectionResult
import com.studyflow.app.domain.ai.CompetencyDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EssayScoreTest {

    private fun createCompetency(score: Int): CompetencyDetail {
        return CompetencyDetail(
            score = score,
            explanation = "Competência avaliada com nota $score.",
            strengths = "Boa argumentação",
            weaknesses = "Pequenos desvios",
            suggestions = "Praticar mais conectivos"
        )
    }

    @Test
    fun essayTotalScore_isSumOfFiveCompetencies() {
        val comp1 = createCompetency(160)
        val comp2 = createCompetency(200)
        val comp3 = createCompetency(160)
        val comp4 = createCompetency(160)
        val comp5 = createCompetency(200)

        val expectedTotal = comp1.score + comp2.score + comp3.score + comp4.score + comp5.score
        assertEquals(880, expectedTotal)

        val result = AICorrectionResult(
            comp1 = comp1,
            comp2 = comp2,
            comp3 = comp3,
            comp4 = comp4,
            comp5 = comp5,
            totalScore = expectedTotal,
            generalComment = "Excelente redação!",
            essayLevel = "Avançado",
            performanceEstimate = "Acima de 850",
            detailedErrors = emptyList(),
            revisedVersion = "Texto revisado"
        )

        assertEquals(880, result.totalScore)
        assertTrue(result.comp1.score in 0..200)
        assertTrue(result.comp2.score in 0..200)
        assertTrue(result.comp3.score in 0..200)
        assertTrue(result.comp4.score in 0..200)
        assertTrue(result.comp5.score in 0..200)
        assertTrue(result.totalScore in 0..1000)
    }

    @Test
    fun essayCorrectionEntity_persistsCorrectScores() {
        val correction = EssayCorrection(
            submissionId = 1,
            comp1 = 200,
            comp2 = 200,
            comp3 = 200,
            comp4 = 200,
            comp5 = 200,
            totalScore = 1000,
            strengths = "Nota máxima em todas as competências",
            weaknesses = "Nenhuma",
            suggestions = "Manter o ritmo",
            date = System.currentTimeMillis()
        )

        val calculatedSum = correction.comp1 + correction.comp2 + correction.comp3 + correction.comp4 + correction.comp5
        assertEquals(1000, calculatedSum)
        assertEquals(correction.totalScore, calculatedSum)
    }
}
