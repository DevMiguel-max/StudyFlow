package com.studyflow.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// ATENÇÃO: Schemas legados das versões 3 e 4 não possuem arquivo JSON de schema versionado.
// Mantemos migrações seguras no-op caso existam bases legadas antes da v5.
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // ATENÇÃO: Schema v3/v4 legado sem deltas estruturais adicionais
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // ATENÇÃO: Schema v4/v5 legado sem deltas estruturais adicionais
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the new study_goals table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `study_goals` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `type` TEXT NOT NULL, 
                `title` TEXT NOT NULL, 
                `description` TEXT, 
                `institution` TEXT, 
                `role` TEXT, 
                `course` TEXT, 
                `certification` TEXT, 
                `language` TEXT, 
                `examBoard` TEXT, 
                `date` INTEGER, 
                `targetScore` REAL, 
                `priority` INTEGER NOT NULL, 
                `status` TEXT NOT NULL
            )
        """)

        // Create notice and document tables
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `exam_notices` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `goalId` INTEGER NOT NULL, `title` TEXT NOT NULL, `institution` TEXT, `role` TEXT, `examBoard` TEXT, `link` TEXT, `publicationDate` INTEGER, `examDate` INTEGER, `phases` INTEGER NOT NULL)
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `exam_notice_versions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `noticeId` INTEGER NOT NULL, `versionNumber` INTEGER NOT NULL, `summaryOfChanges` TEXT, `analysisDate` INTEGER NOT NULL)
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `exam_notice_subjects` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `noticeId` INTEGER NOT NULL, `name` TEXT NOT NULL, `weight` REAL NOT NULL, `questionCount` INTEGER, `category` TEXT)
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `study_documents` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `type` TEXT NOT NULL, `uri` TEXT NOT NULL, `importDate` INTEGER NOT NULL, `isAnalyzed` INTEGER NOT NULL, `relatedGoalId` INTEGER)
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `document_analysis` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `documentId` INTEGER NOT NULL, `extractedText` TEXT, `summary` TEXT, `analysisDate` INTEGER NOT NULL)
        """)

        // Migrate existing ExamGoals to StudyGoals
        database.execSQL("""
            INSERT INTO study_goals (type, title, institution, course, date, targetScore, priority, status)
            SELECT type, name, institution, course, examDate, targetScore, 2, 'active' FROM exam_goals
        """)
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop and recreate achievements table with new schema
        database.execSQL("DROP TABLE IF EXISTS `achievements`")
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `achievements` (
                `id` TEXT NOT NULL, 
                `title` TEXT NOT NULL, 
                `description` TEXT NOT NULL, 
                `icon` TEXT NOT NULL, 
                `tier` TEXT NOT NULL, 
                `category` TEXT NOT NULL, 
                `progress` INTEGER NOT NULL, 
                `maxProgress` INTEGER NOT NULL, 
                `isUnlocked` INTEGER NOT NULL, 
                `unlockedAt` INTEGER, 
                PRIMARY KEY(`id`)
            )
        """)

        // Add new columns to user_profile
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `streakDays` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `maxStreakDays` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `lastStudyDate` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `avatarIcon` TEXT NOT NULL DEFAULT '🧑‍🎓'")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `avatarColor` TEXT NOT NULL DEFAULT '0xFFE0E7FF'")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `gamificationEnabled` INTEGER NOT NULL DEFAULT 1")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `soundsEnabled` INTEGER NOT NULL DEFAULT 1")
        database.execSQL("ALTER TABLE `user_profile` ADD COLUMN `notificationsEnabled` INTEGER NOT NULL DEFAULT 1")

        // Create challenges table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `challenges` (
                `id` TEXT NOT NULL, 
                `title` TEXT NOT NULL, 
                `description` TEXT NOT NULL, 
                `type` TEXT NOT NULL, 
                `requiredAmount` INTEGER NOT NULL, 
                `currentAmount` INTEGER NOT NULL, 
                `xpReward` INTEGER NOT NULL, 
                `isCompleted` INTEGER NOT NULL, 
                `expiresAt` INTEGER NOT NULL, 
                PRIMARY KEY(`id`)
            )
        """)
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val tables = listOf(
            "subjects", "tasks", "study_goals", "study_sessions",
            "review_schedules", "simulations", "essay_submissions",
            "user_profile", "study_plans"
        )
        for (table in tables) {
            db.execSQL("ALTER TABLE $table ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        }
        
        // Add name, email, mainGoal to user_profile
        db.execSQL("ALTER TABLE user_profile ADD COLUMN name TEXT NOT NULL DEFAULT 'Estudante'")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN email TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN mainGoal TEXT NOT NULL DEFAULT 'Aprovação'")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `ai_conversations` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `title` TEXT NOT NULL, 
                `createdAt` INTEGER NOT NULL, 
                `updatedAt` INTEGER NOT NULL, 
                `isArchived` INTEGER NOT NULL, 
                `isFavorite` INTEGER NOT NULL
            )
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `ai_messages` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `conversationId` INTEGER NOT NULL, 
                `text` TEXT NOT NULL, 
                `isUser` INTEGER NOT NULL, 
                `timestamp` INTEGER NOT NULL, 
                `status` TEXT NOT NULL, 
                `isFavorite` INTEGER NOT NULL, 
                FOREIGN KEY(`conversationId`) REFERENCES `ai_conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_messages_conversationId` ON `ai_messages` (`conversationId`)")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Recreate essay_corrections table with new fields
        database.execSQL("DROP TABLE IF EXISTS `essay_corrections`")
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `essay_corrections` (
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
                `date` INTEGER NOT NULL, 
                `generalComment` TEXT NOT NULL DEFAULT '', 
                `essayLevel` TEXT NOT NULL DEFAULT '', 
                `performanceEstimate` TEXT NOT NULL DEFAULT '', 
                `revisedVersion` TEXT NOT NULL DEFAULT '', 
                `detailedAnalysisJson` TEXT NOT NULL DEFAULT '', 
                `competenciesDetailsJson` TEXT NOT NULL DEFAULT '', 
                `modelUsed` TEXT NOT NULL DEFAULT 'meta/llama3-70b-instruct', 
                `isDetailed` INTEGER NOT NULL DEFAULT 1
            )
        """)
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `analyzed_documents` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `title` TEXT NOT NULL, 
                `originalUri` TEXT NOT NULL, 
                `textContent` TEXT NOT NULL, 
                `importDate` INTEGER NOT NULL, 
                `documentType` TEXT NOT NULL, 
                `summaryShort` TEXT, 
                `summaryMedium` TEXT, 
                `summaryComplete` TEXT, 
                `analysisResult` TEXT, 
                `studyPlanId` INTEGER
            )
        """)
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `generated_materials` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `type` TEXT NOT NULL, 
                `title` TEXT NOT NULL, 
                `content` TEXT NOT NULL, 
                `source` TEXT, 
                `createdAt` INTEGER NOT NULL
            )
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `generated_questions` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `type` TEXT NOT NULL, 
                `statement` TEXT NOT NULL, 
                `options` TEXT NOT NULL, 
                `correctAnswer` TEXT NOT NULL, 
                `explanation` TEXT NOT NULL, 
                `difficulty` TEXT NOT NULL, 
                `subject` TEXT NOT NULL, 
                `topic` TEXT NOT NULL, 
                `source` TEXT, 
                `isAnswered` INTEGER NOT NULL, 
                `wasCorrect` INTEGER NOT NULL, 
                `createdAt` INTEGER NOT NULL
            )
        """)
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `mentor_profile` (
                `id` INTEGER NOT NULL, 
                `mainGoal` TEXT NOT NULL, 
                `examType` TEXT NOT NULL, 
                `examDate` INTEGER NOT NULL, 
                `hoursPerDay` INTEGER NOT NULL, 
                `availableDays` TEXT NOT NULL, 
                `preferredTime` TEXT NOT NULL, 
                `favoriteSubjects` TEXT NOT NULL, 
                `difficultSubjects` TEXT NOT NULL, 
                `preferredStudyMethod` TEXT NOT NULL, 
                `learningPace` TEXT NOT NULL, 
                `coachModeEnabled` INTEGER NOT NULL, 
                `mentorEnabled` INTEGER NOT NULL, 
                PRIMARY KEY(`id`)
            )
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `mentor_recommendation` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `text` TEXT NOT NULL, 
                `reason` TEXT NOT NULL, 
                `type` TEXT NOT NULL, 
                `timestamp` INTEGER NOT NULL, 
                `isRead` INTEGER NOT NULL, 
                `isAccepted` INTEGER
            )
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `smart_mission` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `title` TEXT NOT NULL, 
                `description` TEXT NOT NULL, 
                `xpReward` INTEGER NOT NULL, 
                `isCompleted` INTEGER NOT NULL, 
                `type` TEXT NOT NULL, 
                `targetAmount` INTEGER NOT NULL, 
                `currentAmount` INTEGER NOT NULL, 
                `timestamp` INTEGER NOT NULL
            )
        """)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `mentor_history` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                `eventType` TEXT NOT NULL, 
                `details` TEXT NOT NULL, 
                `timestamp` INTEGER NOT NULL
            )
        """)
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `ai_cache` (
                `cacheKey` TEXT NOT NULL, 
                `content` TEXT NOT NULL, 
                `timestamp` INTEGER NOT NULL, 
                `type` TEXT NOT NULL, 
                PRIMARY KEY(`cacheKey`)
            )
        """)
    }
}

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
    MIGRATION_7_8,
    MIGRATION_8_9,
    MIGRATION_9_10,
    MIGRATION_10_11,
    MIGRATION_11_12,
    MIGRATION_12_13,
    MIGRATION_13_14
)
