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

    /**
     * Plano B para MIGRATION_6_7 (Sem schema v6):
     * Verifica que a migração 6->7 preserva conquistas existentes do usuário.
     */
    @Test
    fun migration6To7_preservesExistingAchievements() {
        val db = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(
                InstrumentationRegistry.getInstrumentation().targetContext
            ).name("test_v6_v7.db").callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(6) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE `achievements` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                    db.execSQL("CREATE TABLE `user_profile` (`id` INTEGER PRIMARY KEY NOT NULL, `xp` INTEGER NOT NULL, `level` INTEGER NOT NULL)")
                }
                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            }).build()
        ).writableDatabase

        db.execSQL("INSERT INTO achievements (id, name) VALUES (1, 'Primeira Conquista')")
        db.execSQL("INSERT INTO user_profile (id, xp, level) VALUES (1, 100, 2)")

        MIGRATION_6_7.migrate(db)

        val cursor = db.query("SELECT id, title, icon, tier FROM achievements WHERE id = '1'")
        assertTrue(cursor.moveToFirst())
        assertEquals("1", cursor.getString(0))
        assertEquals("Primeira Conquista", cursor.getString(1))
        cursor.close()

        val profileCursor = db.query("SELECT streakDays, avatarIcon FROM user_profile WHERE id = 1")
        assertTrue(profileCursor.moveToFirst())
        assertEquals(0, profileCursor.getInt(0))
        profileCursor.close()
        db.close()
    }

    /**
     * Plano B para MIGRATION_8_9 (Sem schema v8):
     * Cria tabelas v8, executa migração e valida criação de ai_conversations e ai_messages.
     */
    @Test
    fun migration8To9_createsAIChatTablesAndPersistsData() {
        val db = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(
                InstrumentationRegistry.getInstrumentation().targetContext
            ).name("test_v8_v9.db").callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(8) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE `subjects` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL DEFAULT 0)")
                }
                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            }).build()
        ).writableDatabase

        db.execSQL("INSERT INTO subjects (id, name, updatedAt) VALUES (1, 'Português', 1000)")

        MIGRATION_8_9.migrate(db)

        // Verifica que tabelas novas foram criadas
        val convCheck = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='ai_conversations'")
        assertTrue(convCheck.moveToFirst())
        convCheck.close()

        val msgCheck = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='ai_messages'")
        assertTrue(msgCheck.moveToFirst())
        msgCheck.close()

        // Verifica que dados pré-existentes persistem
        val subCheck = db.query("SELECT id, name FROM subjects WHERE id = 1")
        assertTrue(subCheck.moveToFirst())
        assertEquals("Português", subCheck.getString(1))
        subCheck.close()
        db.close()
    }

    /**
     * Plano B para MIGRATION_9_10:
     * Verifica que a migração 9->10 preserva redações corrigidas pré-existentes.
     */
    @Test
    fun migration9To10_preservesExistingEssayCorrections() {
        val db = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(
                InstrumentationRegistry.getInstrumentation().targetContext
            ).name("test_v9_v10.db").callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(9) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE `essay_corrections` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `submissionId` INTEGER NOT NULL,
                            `comp1` INTEGER NOT NULL,
                            `comp2` INTEGER NOT NULL,
                            `comp3` INTEGER NOT NULL,
                            `comp4` INTEGER NOT NULL,
                            `comp5` INTEGER NOT NULL,
                            `totalScore` INTEGER NOT NULL,
                            `strengths` TEXT NOT NULL,
                            `weaknesses` TEXT NOT NULL,
                            `suggestions` TEXT NOT NULL,
                            `date` INTEGER NOT NULL
                        )
                    """.trimIndent())
                }
                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            }).build()
        ).writableDatabase

        db.execSQL("""
            INSERT INTO essay_corrections (id, submissionId, comp1, comp2, comp3, comp4, comp5, totalScore, strengths, weaknesses, suggestions, date)
            VALUES (1, 10, 160, 160, 160, 160, 200, 840, 'Bom vocabulário', 'Pontuação', 'Revisar vírgulas', 5000)
        """.trimIndent())

        MIGRATION_9_10.migrate(db)

        val cursor = db.query("SELECT id, totalScore, generalComment, modelUsed FROM essay_corrections WHERE id = 1")
        assertTrue("Linha de redação deve persistir após migração 9->10", cursor.moveToFirst())
        assertEquals(1, cursor.getInt(0))
        assertEquals(840, cursor.getInt(1))
        assertEquals("", cursor.getString(2))
        assertEquals("gemini-2.5-flash", cursor.getString(3))
        cursor.close()
        db.close()
    }

    /**
     * Plano B para MIGRATION_10_11:
     * Valida criação da tabela analyzed_documents e persistência dos dados anteriores.
     */
    @Test
    fun migration10To11_createsAnalyzedDocumentsAndPersistsData() {
        val db = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(
                InstrumentationRegistry.getInstrumentation().targetContext
            ).name("test_v10_v11.db").callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(10) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE `subjects` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                }
                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            }).build()
        ).writableDatabase

        db.execSQL("INSERT INTO subjects (id, name) VALUES (1, 'História')")

        MIGRATION_10_11.migrate(db)

        val docCheck = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='analyzed_documents'")
        assertTrue(docCheck.moveToFirst())
        docCheck.close()

        val subCheck = db.query("SELECT name FROM subjects WHERE id = 1")
        assertTrue(subCheck.moveToFirst())
        assertEquals("História", subCheck.getString(0))
        subCheck.close()
        db.close()
    }

    /**
     * Plano B para MIGRATION_11_12:
     * Valida criação de generated_materials e generated_questions.
     */
    @Test
    fun migration11To12_createsGeneratedMaterialsAndQuestions() {
        val db = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(
                InstrumentationRegistry.getInstrumentation().targetContext
            ).name("test_v11_v12.db").callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(11) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE `subjects` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                }
                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            }).build()
        ).writableDatabase

        db.execSQL("INSERT INTO subjects (id, name) VALUES (1, 'Química')")

        MIGRATION_11_12.migrate(db)

        val matCheck = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='generated_materials'")
        assertTrue(matCheck.moveToFirst())
        matCheck.close()

        val qCheck = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='generated_questions'")
        assertTrue(qCheck.moveToFirst())
        qCheck.close()

        val subCheck = db.query("SELECT name FROM subjects WHERE id = 1")
        assertTrue(subCheck.moveToFirst())
        assertEquals("Química", subCheck.getString(0))
        subCheck.close()
        db.close()
    }

    /**
     * Teste com schema JSON oficial da menor versão disponível (v12) até a v14 usando ALL_MIGRATIONS.
     */
    @Test
    fun fullChainMigration_12To14_withSchemaValidation() {
        var db = helper.createDatabase(TEST_DB, 12).apply {
            execSQL("""
                INSERT INTO subjects (id, updatedAt, name, color, icon, professor, description, difficulty, weeklyHoursTarget, status, createdAt, isFavorite, examContent, availableHoursPerDay, availableDaysOfWeek, studyGoal)
                VALUES (2, 2000, 'Física', '0xFF7C3AED', '⚛️', 'Prof. Newton', 'Mecânica', 4, 8, 'active', 2000, 0, 'Cinemática', 1.5, 'QUA,QUI', 'Vestibular')
            """.trimIndent())
            close()
        }

        db = helper.runMigrationsAndValidate(TEST_DB, 14, true, *ALL_MIGRATIONS)

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
}
