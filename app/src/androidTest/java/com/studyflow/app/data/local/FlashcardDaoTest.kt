package com.studyflow.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlashcardDaoTest {

    private lateinit var database: StudyFlowDatabase
    private lateinit var dao: StudyFlowDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, StudyFlowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.studyFlowDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun getDueFlashcards_returnsOnlyOverdueCardsOrderedByNextReviewDate() = runBlocking {
        val now = 1_000_000L
        val past1 = Flashcard(id = 1, subjectId = 1, question = "Q1", answer = "A1", box = 1, nextReviewDate = now - 200)
        val past2 = Flashcard(id = 2, subjectId = 1, question = "Q2", answer = "A2", box = 2, nextReviewDate = now - 500)
        val future = Flashcard(id = 3, subjectId = 1, question = "Q3", answer = "A3", box = 3, nextReviewDate = now + 1000)

        dao.insertFlashcard(past1)
        dao.insertFlashcard(past2)
        dao.insertFlashcard(future)

        val due = dao.getDueFlashcards(now).first()
        assertEquals(2, due.size)
        // Ordenado pelo mais antigo (past2 com nextReviewDate menor vem primeiro)
        assertEquals(2, due[0].id)
        assertEquals(1, due[1].id)
    }

    @Test
    fun getDueFlashcardsCount_returnsAccurateCount() = runBlocking {
        val now = 1_000_000L
        val card1 = Flashcard(id = 1, subjectId = 1, question = "Q1", answer = "A1", box = 1, nextReviewDate = now - 100)
        val card2 = Flashcard(id = 2, subjectId = 1, question = "Q2", answer = "A2", box = 1, nextReviewDate = now)
        val card3 = Flashcard(id = 3, subjectId = 1, question = "Q3", answer = "A3", box = 1, nextReviewDate = now + 500)

        dao.insertFlashcard(card1)
        dao.insertFlashcard(card2)
        dao.insertFlashcard(card3)

        val count = dao.getDueFlashcardsCount(now).first()
        assertEquals(2, count)
    }

    @Test
    fun getFlashcardCountsByBox_groupsCorrectly() = runBlocking {
        val now = 1_000_000L
        dao.insertFlashcard(Flashcard(id = 1, subjectId = 1, question = "Q1", answer = "A1", box = 1, nextReviewDate = now))
        dao.insertFlashcard(Flashcard(id = 2, subjectId = 1, question = "Q2", answer = "A2", box = 1, nextReviewDate = now))
        dao.insertFlashcard(Flashcard(id = 3, subjectId = 1, question = "Q3", answer = "A3", box = 2, nextReviewDate = now))
        dao.insertFlashcard(Flashcard(id = 4, subjectId = 1, question = "Q4", answer = "A4", box = 3, nextReviewDate = now))

        val boxCounts = dao.getFlashcardCountsByBox().first()
        val boxMap = boxCounts.associate { it.box to it.count }

        assertEquals(2, boxMap[1])
        assertEquals(1, boxMap[2])
        assertEquals(1, boxMap[3])
    }
}
