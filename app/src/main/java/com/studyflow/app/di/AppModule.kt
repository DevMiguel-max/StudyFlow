package com.studyflow.app.di

import androidx.room.Room
import com.studyflow.app.data.local.StudyFlowDatabase
import com.studyflow.app.data.local.ALL_MIGRATIONS
import com.studyflow.app.data.repository.StudyFlowRepositoryImpl
import com.studyflow.app.domain.repository.StudyFlowRepository
import com.studyflow.app.presentation.viewmodel.CalendarViewModel
import com.studyflow.app.presentation.viewmodel.StatisticsViewModel
import com.studyflow.app.presentation.viewmodel.ProfileViewModel
import com.studyflow.app.presentation.viewmodel.AuthViewModel
import com.studyflow.app.presentation.viewmodel.SyncViewModel
import com.studyflow.app.presentation.viewmodel.HomeViewModel
import com.studyflow.app.presentation.viewmodel.StudyPlanViewModel
import com.studyflow.app.presentation.viewmodel.SubjectViewModel
import com.studyflow.app.presentation.viewmodel.TaskViewModel
import com.studyflow.app.presentation.viewmodel.StudySessionViewModel
import com.studyflow.app.presentation.viewmodel.PreparationViewModel
import com.studyflow.app.presentation.viewmodel.EssayViewModel
import com.studyflow.app.presentation.viewmodel.SimulationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.studyflow.app.domain.repository.AuthRepository
import com.studyflow.app.domain.repository.SyncRepository
import com.studyflow.app.data.repository.AuthRepositoryImpl
import com.studyflow.app.data.repository.SyncRepositoryImpl
import org.koin.dsl.module

val appModule = module {
    single { com.studyflow.app.domain.ai.GeminiClient() }
    single { com.studyflow.app.domain.ai.MentorService(get()) }
    single { com.studyflow.app.domain.ai.DocumentAnalyzerService(get()) }
    single { com.studyflow.app.domain.ai.MaterialGeneratorService(get()) }
    single { com.studyflow.app.domain.ai.AIEssayCorrectorService(get()) }
    single { com.studyflow.app.domain.ai.AITutorService(get()) }
    single { com.studyflow.app.domain.ai.StudyTipService(get()) }
    single<com.studyflow.app.domain.repository.MaterialGeneratorRepository> { com.studyflow.app.data.repository.MaterialGeneratorRepositoryImpl(get()) }
    viewModel { com.studyflow.app.presentation.viewmodel.MaterialGeneratorViewModel(get(), get(), get(), get()) }
    single<com.studyflow.app.domain.repository.DocumentAnalyzerRepository> { com.studyflow.app.data.repository.DocumentAnalyzerRepositoryImpl(get()) }
    viewModel { com.studyflow.app.presentation.viewmodel.DocumentAnalyzerViewModel(get(), get()) }
    single {
        Room.databaseBuilder(
            androidContext(),
            StudyFlowDatabase::class.java,
            "studyflow.db"
        )
        .addMigrations(*ALL_MIGRATIONS)
        .build()
    }
    
    single { get<StudyFlowDatabase>().studyFlowDao() }
    single { get<StudyFlowDatabase>().aiCacheDao() }
    single { com.studyflow.app.domain.ai.AIMetadataManager(androidContext()) }
    
    single { FirebaseAuth.getInstance() }
    single {
        val dbId = androidContext().getString(com.studyflow.app.R.string.firestore_database_id)
        if (dbId.isBlank() || dbId == "(default)") {
            FirebaseFirestore.getInstance()
        } else {
            FirebaseFirestore.getInstance(dbId)
        }
    }
    
    single<AuthRepository> { AuthRepositoryImpl(get(), androidContext()) }
    single<SyncRepository> { SyncRepositoryImpl(get(), get(), get<com.studyflow.app.data.local.StudyFlowDatabase>().studyFlowDao()) }
    single<com.studyflow.app.domain.repository.MentorRepository> { com.studyflow.app.data.repository.MentorRepositoryImpl(get()) }
    single<StudyFlowRepository> { StudyFlowRepositoryImpl(get()) }
    single { com.studyflow.app.domain.manager.GamificationManager(get()) }
    
    viewModel { com.studyflow.app.presentation.viewmodel.MentorViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
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
    viewModel { com.studyflow.app.presentation.viewmodel.AITutorViewModel(get()) }
}
