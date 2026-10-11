package com.studyflow.app.domain.util

import com.studyflow.app.data.local.Flashcard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LeitnerSchedulerTest {

    private val baseNow = 1_700_000_000_000L // Timestamp fixo de teste

    private fun createCard(id: Int = 1, box: Int = 1, nextReviewDate: Long = baseNow): Flashcard {
        return Flashcard(
            id = id,
            subjectId = 10,
            question = "Pergunta $id",
            answer = "Resposta $id",
            box = box,
            nextReviewDate = nextReviewDate
        )
    }

    @Test
    fun next_whenBox1AndCorrect_promotesToBox2AndAddsThreeDays() {
        val card = createCard(box = 1)

        val updated = LeitnerScheduler.next(card, correct = true, nowMillis = baseNow)

        assertEquals(2, updated.box)
        val expectedDate = baseNow + LeitnerScheduler.THREE_DAYS_MILLIS
        assertEquals(expectedDate, updated.nextReviewDate)
    }

    @Test
    fun next_whenBox2AndCorrect_promotesToBox3AndAddsSevenDays() {
        val card = createCard(box = 2)

        val updated = LeitnerScheduler.next(card, correct = true, nowMillis = baseNow)

        assertEquals(3, updated.box)
        val expectedDate = baseNow + LeitnerScheduler.SEVEN_DAYS_MILLIS
        assertEquals(expectedDate, updated.nextReviewDate)
    }

    @Test
    fun next_whenBox3AndCorrect_remainsInBox3AndReschedulesInSevenDays() {
        val card = createCard(box = 3)

        val updated = LeitnerScheduler.next(card, correct = true, nowMillis = baseNow)

        assertEquals(3, updated.box)
        val expectedDate = baseNow + LeitnerScheduler.SEVEN_DAYS_MILLIS
        assertEquals(expectedDate, updated.nextReviewDate)
    }

    @Test
    fun next_whenIncorrectInBox1_staysInBox1AndAddsOneDay() {
        val card = createCard(box = 1)

        val updated = LeitnerScheduler.next(card, correct = false, nowMillis = baseNow)

        assertEquals(1, updated.box)
        val expectedDate = baseNow + LeitnerScheduler.ONE_DAY_MILLIS
        assertEquals(expectedDate, updated.nextReviewDate)
    }

    @Test
    fun next_whenIncorrectInBox2_returnsToBox1AndAddsOneDay() {
        val card = createCard(box = 2)

        val updated = LeitnerScheduler.next(card, correct = false, nowMillis = baseNow)

        assertEquals(1, updated.box)
        val expectedDate = baseNow + LeitnerScheduler.ONE_DAY_MILLIS
        assertEquals(expectedDate, updated.nextReviewDate)
    }

    @Test
    fun next_whenIncorrectInBox3_returnsToBox1AndAddsOneDay() {
        val card = createCard(box = 3)

        val updated = LeitnerScheduler.next(card, correct = false, nowMillis = baseNow)

        assertEquals(1, updated.box)
        val expectedDate = baseNow + LeitnerScheduler.ONE_DAY_MILLIS
        assertEquals(expectedDate, updated.nextReviewDate)
    }

    @Test
    fun isDue_returnsTrueOnlyWhenNextReviewDateIsLessOrEqualToNow() {
        val pastDueCard = createCard(nextReviewDate = baseNow - 1000L)
        val exactDueCard = createCard(nextReviewDate = baseNow)
        val futureCard = createCard(nextReviewDate = baseNow + 1000L)

        assertTrue("Cartão com data no passado deve estar vencido", LeitnerScheduler.isDue(pastDueCard, baseNow))
        assertTrue("Cartão com data exata deve estar vencido", LeitnerScheduler.isDue(exactDueCard, baseNow))
        assertFalse("Cartão com data no futuro NÃO deve estar vencido", LeitnerScheduler.isDue(futureCard, baseNow))
    }
}
