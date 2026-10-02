package com.studyflow.app

import com.google.firebase.FirebaseApp
import android.app.Application
import com.studyflow.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class StudyFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.studyflow.app.domain.util.DocumentTextExtractor.init(this)
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // Might be already initialized
        }
        AppCheckInitializer.init()

        if (org.koin.core.context.GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@StudyFlowApplication)
                modules(appModule)
            }
        }
    }
}
