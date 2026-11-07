package com.studyflow.app

import com.google.firebase.FirebaseApp
import android.app.Application
import com.studyflow.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import kotlinx.coroutines.launch

class StudyFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.studyflow.app.domain.util.DocumentTextExtractor.init(this)
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // Might be already initialized
        }

        if (org.koin.core.context.GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@StudyFlowApplication)
                modules(appModule)
            }
        }

        // LGPD: limpeza de retenção do cache de IA na abertura do app (expira em 30 dias)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                com.studyflow.app.domain.ai.AICacheManager.purgeExpiredCache()
            } catch (e: Exception) {
                android.util.Log.w("StudyFlowApp", "Erro ao expirar cache de IA", e)
            }
        }
    }
}
