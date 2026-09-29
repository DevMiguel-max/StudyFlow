package com.studyflow.app.presentation.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Interactive Google Sign-In using Jetpack Credential Manager.
 * Uses GetSignInWithGoogleOption for presenting the Google account chooser / sign-in dialog.
 */
suspend fun signInWithGoogle(context: Context, webClientId: String): String? {
    return withContext(Dispatchers.Main) {
        val activity = context as? Activity
        if (activity == null) {
            Log.e("Auth", "Context is not an Activity; cannot launch Google Sign-In")
            return@withContext null
        }

        try {
            val credentialManager = CredentialManager.create(activity)
            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                return@withContext googleIdTokenCredential.idToken
            } else {
                Log.e("Auth", "Unexpected credential type: ${credential.type}")
                return@withContext null
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            return@withContext null
        } catch (e: NoCredentialException) {
            Log.w("Auth", "No credential available: ${e.message}", e)
            Toast.makeText(context, "Nenhuma conta Google encontrada no dispositivo", Toast.LENGTH_SHORT).show()
            return@withContext null
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            val message = e.localizedMessage ?: "Falha ao autenticar com o Google"
            Toast.makeText(context, "Erro no login Google: $message", Toast.LENGTH_LONG).show()
            return@withContext null
        }
    }
}

/**
 * Silent auto-sign-in helper for background startup checks.
 */
suspend fun attemptSilentSignIn(context: Context, webClientId: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                googleIdTokenCredential.idToken
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d("Auth", "Silent auto-sign-in was not possible: ${e.message}")
            null
        }
    }
}
