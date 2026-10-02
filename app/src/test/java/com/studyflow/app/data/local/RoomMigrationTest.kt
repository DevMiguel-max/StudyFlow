package com.studyflow.app.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        StudyFlowDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate13To14_dataPersists() {
        var db = helper.createDatabase(TEST_DB, 13).apply {
            execSQL("""
                INSERT INTO subjects (id, updatedAt, name, color, icon, professor, description, difficulty, weeklyHoursTarget, status, createdAt, isFavorite, examContent, availableHoursPerDay, availableDaysOfWeek, studyGoal)
                VALUES (1, 1000, 'Matemática', '0xFF2563EB', '📐', 'Prof. Euler', 'Cálculo', 3, 10, 'active', 1000, 1, 'Geometria', 2.0, 'SEG,TER', 'ENEM')
            """.trimIndent())
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 14, true, MIGRATION_13_14)

        val cursor = db.query("SELECT id, name FROM subjects WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals(1, cursor.getInt(0))
        assertEquals("Matemática", cursor.getString(1))
        cursor.close()

        val cacheCursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='ai_cache'")
        assertTrue(cacheCursor.moveToFirst())
        cacheCursor.close()
    }

    @Test
    fun migrate12To14_dataPersists() {
        var db = helper.createDatabase(TEST_DB, 12).apply {
            execSQL("""
                INSERT INTO subjects (id, updatedAt, name, color, icon, professor, description, difficulty, weeklyHoursTarget, status, createdAt, isFavorite, examContent, availableHoursPerDay, availableDaysOfWeek, studyGoal)
                VALUES (2, 2000, 'Física', '0xFF7C3AED', '⚛️', 'Prof. Newton', 'Mecânica', 4, 8, 'active', 2000, 0, 'Cinemática', 1.5, 'QUA,QUI', 'Vestibular')
            """.trimIndent())
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 14, true, MIGRATION_12_13, MIGRATION_13_14)

        val cursor = db.query("SELECT id, name FROM subjects WHERE id = 2")
        assertTrue(cursor.moveToFirst())
        assertEquals(2, cursor.getInt(0))
        assertEquals("Física", cursor.getString(1))
        cursor.close()

        val mentorCursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='mentor_profile'")
        assertTrue(mentorCursor.moveToFirst())
        mentorCursor.close()

        val cacheCursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='ai_cache'")
        assertTrue(cacheCursor.moveToFirst())
        cacheCursor.close()
    }
}
