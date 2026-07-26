package com.example.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.StudyFlowDatabase
import com.example.data.repository.StudyFlowRepositoryImpl
import com.example.domain.repository.StudyFlowRepository
import com.example.presentation.viewmodel.CalendarViewModel
import com.example.presentation.viewmodel.StatisticsViewModel
import com.example.presentation.viewmodel.ProfileViewModel
import com.example.presentation.viewmodel.AuthViewModel
import com.example.presentation.viewmodel.SyncViewModel
import com.example.presentation.viewmodel.HomeViewModel
import com.example.presentation.viewmodel.StudyPlanViewModel
import com.example.presentation.viewmodel.SubjectViewModel
import com.example.presentation.viewmodel.TaskViewModel
import com.example.presentation.viewmodel.StudySessionViewModel
import com.example.presentation.viewmodel.PreparationViewModel
import com.example.presentation.viewmodel.EssayViewModel
import com.example.presentation.viewmodel.SimulationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.SyncRepository
import com.example.data.repository.AuthRepositoryImpl
import com.example.data.repository.SyncRepositoryImpl
import org.koin.dsl.module

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

        // Create other new tables
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
                `modelUsed` TEXT NOT NULL DEFAULT 'gemini-1.5-pro-latest',
                `isDetailed` INTEGER NOT NULL DEFAULT 1
            )
        """)
    }
}

val appModule = module {
    single { com.example.domain.ai.MentorService() }
    single { com.example.domain.ai.DocumentAnalyzerService() }
    single { com.example.domain.ai.MaterialGeneratorService() }
    single<com.example.domain.repository.MaterialGeneratorRepository> { com.example.data.repository.MaterialGeneratorRepositoryImpl(get()) }
    viewModel { com.example.presentation.viewmodel.MaterialGeneratorViewModel(get(), get(), get(), get()) }
    single<com.example.domain.repository.DocumentAnalyzerRepository> { com.example.data.repository.DocumentAnalyzerRepositoryImpl(get()) }
    viewModel { com.example.presentation.viewmodel.DocumentAnalyzerViewModel(get(), get()) }
    single {
        Room.databaseBuilder(
            androidContext(),
            StudyFlowDatabase::class.java,
            "studyflow.db"
        )
        .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
        .addMigrations(com.example.data.local.MIGRATION_7_8, MIGRATION_9_10, com.example.di.MIGRATION_12_13)
            .fallbackToDestructiveMigration()
        .build()
    }
    
    single { get<StudyFlowDatabase>().studyFlowDao() }
    
        single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<SyncRepository> { SyncRepositoryImpl(get(), get(), get<com.example.data.local.StudyFlowDatabase>().studyFlowDao()) }
    single<com.example.domain.repository.MentorRepository> { com.example.data.repository.MentorRepositoryImpl(get()) }
    single<StudyFlowRepository> { StudyFlowRepositoryImpl(get()) }
    single { com.example.domain.manager.GamificationManager(get()) }
    
    viewModel { com.example.presentation.viewmodel.MentorViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { SubjectViewModel(get()) }
    viewModel { TaskViewModel(get(), get()) }
    viewModel { CalendarViewModel(get()) }
    viewModel { StudyPlanViewModel(get()) }
    viewModel { StudySessionViewModel(get(), get()) }
    viewModel { PreparationViewModel(get()) }
    viewModel { EssayViewModel(get(), get()) }
    viewModel { SimulationViewModel(get(), get()) }
    viewModel { StatisticsViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { SyncViewModel(get()) }
    viewModel { com.example.presentation.viewmodel.AITutorViewModel(get()) }
}
