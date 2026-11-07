package com.studyflow.app

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

object AppCheckInitializer {
    private const val TAG = "AppCheckInitializer"
    private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
    private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
    private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"

    fun init(context: Context? = null, intent: Intent? = null) {
        if (context == null) return
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }

            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
                ?: intent?.extras?.getString(DEBUG_TOKEN_EXTRA)
                ?: intent?.getStringExtra("firebase_app_check_debug_token")
                ?: intent?.extras?.getString("firebase_app_check_debug_token")
                ?: System.getProperty(DEBUG_TOKEN_EXTRA)
                ?: System.getenv(DEBUG_TOKEN_EXTRA)

            if (!debugToken.isNullOrBlank()) {
                val app = FirebaseApp.getInstance()
                val prefsName = "$DEBUG_STORE_PREFIX${app.persistenceKey}"
                context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                    .edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(TAG, "Configured debug App Check token from intent/env successfully.")
            } else {
                Log.w(TAG, "No debug token found in Intent extra.")
            }

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase App Check", e)
        }
    }
}
