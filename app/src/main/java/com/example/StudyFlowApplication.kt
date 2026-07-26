package com.example

import com.google.firebase.FirebaseApp
import android.app.Application
import com.example.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class StudyFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.example.domain.util.DocumentTextExtractor.init(this)
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // Might be already initialized
        }

        startKoin {
            androidContext(this@StudyFlowApplication)
            modules(appModule)
        }
    }
}
