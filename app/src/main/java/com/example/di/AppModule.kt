package com.example.di

import androidx.room.Room
import com.example.data.local.StudyFlowDatabase
import com.example.data.repository.StudyFlowRepositoryImpl
import com.example.domain.repository.StudyFlowRepository
import com.example.presentation.viewmodel.CalendarViewModel
import com.example.presentation.viewmodel.HomeViewModel
import com.example.presentation.viewmodel.StudyPlanViewModel
import com.example.presentation.viewmodel.SubjectViewModel
import com.example.presentation.viewmodel.TaskViewModel
import com.example.presentation.viewmodel.StudySessionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            StudyFlowDatabase::class.java,
            "studyflow.db"
        ).fallbackToDestructiveMigration().build()
    }
    
    single { get<StudyFlowDatabase>().studyFlowDao() }
    
    single<StudyFlowRepository> { StudyFlowRepositoryImpl(get()) }
    
    viewModel { HomeViewModel(get()) }
    viewModel { SubjectViewModel(get()) }
    viewModel { TaskViewModel(get()) }
    viewModel { CalendarViewModel(get()) }
    viewModel { StudyPlanViewModel(get()) }
    viewModel { StudySessionViewModel(get()) }
}
