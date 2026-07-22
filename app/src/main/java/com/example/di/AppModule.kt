package com.example.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.StudyFlowDatabase
import com.example.data.repository.StudyFlowRepositoryImpl
import com.example.domain.repository.StudyFlowRepository
import com.example.presentation.viewmodel.CalendarViewModel
import com.example.presentation.viewmodel.StatisticsViewModel
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

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            StudyFlowDatabase::class.java,
            "studyflow.db"
        )
        .addMigrations(MIGRATION_5_6)
        .fallbackToDestructiveMigration()
        .build()
    }
    
    single { get<StudyFlowDatabase>().studyFlowDao() }
    
    single<StudyFlowRepository> { StudyFlowRepositoryImpl(get()) }
    
    viewModel { HomeViewModel(get()) }
    viewModel { SubjectViewModel(get()) }
    viewModel { TaskViewModel(get()) }
    viewModel { CalendarViewModel(get()) }
    viewModel { StudyPlanViewModel(get()) }
    viewModel { StudySessionViewModel(get()) }
    viewModel { PreparationViewModel(get()) }
    viewModel { EssayViewModel(get()) }
    viewModel { SimulationViewModel(get()) }
    viewModel { StatisticsViewModel(get()) }
}
