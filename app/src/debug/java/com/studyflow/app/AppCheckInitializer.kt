package com.studyflow.app

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

object AppCheckInitializer {
    private const val TAG = "AppCheckInitializer"

    fun init(context: Context? = null, intent: Intent? = null) {
        val debugToken = intent?.getStringExtra("FIREBASE_APPCHECK_DEBUG_TOKEN")
            ?: System.getProperty("FIREBASE_APPCHECK_DEBUG_TOKEN")
            ?: System.getenv("FIREBASE_APPCHECK_DEBUG_TOKEN")

        if (context != null && !debugToken.isNullOrBlank()) {
            try {
                val app = FirebaseApp.getInstance()
                val prefsName = "com.google.firebase.appcheck.debug.store." + app.persistenceKey
                context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                    .edit()
                    .putString("com.google.firebase.appcheck.debug.DEBUG_SECRET", debugToken)
                    .commit()
                Log.d(TAG, "Configured debug App Check token from intent/env")
            } catch (e: Exception) {
                Log.w(TAG, "Could not store debug secret", e)
            }
        }

        try {
            Firebase.appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } catch (e: Exception) {
            Log.w(TAG, "AppCheck factory installation error", e)
        }
    }
}
