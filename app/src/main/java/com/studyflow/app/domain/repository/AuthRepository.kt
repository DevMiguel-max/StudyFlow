package com.studyflow.app.domain.repository

import kotlinx.coroutines.flow.Flow

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    
    suspend fun signInWithEmail(email: String, pass: String): Result<AuthUser>
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signUpWithEmail(email: String, pass: String): Result<AuthUser>
    suspend fun recoverPassword(email: String): Result<Unit>
    suspend fun signOut()
    suspend fun deleteAccount(): Result<Unit>
}
