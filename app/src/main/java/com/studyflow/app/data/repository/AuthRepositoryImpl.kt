package com.studyflow.app.data.repository

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.studyflow.app.domain.repository.AuthRepository
import com.studyflow.app.domain.repository.AuthUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val context: Context? = null
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                trySend(AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString()))
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, pass: String): Result<AuthUser> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
        val user = result.user ?: throw Exception("User null")
        AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString())
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw Exception("User null")
        AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString())
    }

    override suspend fun signUpWithEmail(email: String, pass: String): Result<AuthUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
        val user = result.user ?: throw Exception("User null")
        AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString())
    }

    override suspend fun recoverPassword(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun signOut() {
        auth.signOut()
        context?.let { ctx ->
            try {
                val credentialManager = CredentialManager.create(ctx)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                // Ignore credential clearing issues on logout
            }
        }
    }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        auth.currentUser?.delete()?.await()
        context?.let { ctx ->
            try {
                val credentialManager = CredentialManager.create(ctx)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                // Ignore
            }
        }
        Unit
    }
}
