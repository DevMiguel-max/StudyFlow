package com.studyflow.app.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.studyflow.app.data.local.StudyFlowDatabase
import com.studyflow.app.domain.manager.PrivacyManager
import com.studyflow.app.domain.repository.AuthRepository
import com.studyflow.app.domain.repository.AuthUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore? = null,
    private val database: StudyFlowDatabase? = null,
    private val privacyManager: PrivacyManager? = null,
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
        val user = result.user ?: throw Exception("Usuário inválido ou nulo.")
        AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString())
    }

    override suspend fun signUpWithEmail(email: String, pass: String): Result<AuthUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
        val user = result.user ?: throw Exception("Usuário inválido ou nulo.")
        AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString())
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw Exception("Usuário inválido ou nulo.")
        AuthUser(user.uid, user.email, user.displayName, user.photoUrl?.toString())
    }

    override suspend fun reauthenticateWithEmail(password: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("Nenhum usuário conectado.")
        val email = user.email ?: throw IllegalStateException("E-mail não encontrado para o usuário.")
        val credential = EmailAuthProvider.getCredential(email.trim(), password)
        user.reauthenticate(credential).await()
        Unit
    }

    override suspend fun reauthenticateWithGoogle(idToken: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("Nenhum usuário conectado.")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        user.reauthenticate(credential).await()
        Unit
    }

    override suspend fun recoverPassword(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val user = auth.currentUser ?: throw IllegalStateException("Nenhum usuário autenticado.")
            val uid = user.uid

            // (a) Apagar no Firestore users/{uid} e TODAS as subcoleções que o SyncRepositoryImpl cria em lotes
            firestore?.let { fs ->
                val userDocRef = fs.collection("users").document(uid)
                val subcollections = listOf(
                    "subjects", "tasks", "study_goals", "study_sessions",
                    "flashcards", "study_notes", "mind_map_nodes", "essay_submissions",
                    "simulations", "achievements", "challenges", "generated_materials",
                    "generated_questions", "study_plans", "exam_goals"
                )
                for (subName in subcollections) {
                    try {
                        val subRef = userDocRef.collection(subName)
                        val snapshot = subRef.limit(500).get().await()
                        if (!snapshot.isEmpty) {
                            val batch = fs.batch()
                            for (doc in snapshot.documents) {
                                batch.delete(doc.reference)
                            }
                            batch.commit().await()
                        }
                    } catch (e: Exception) {
                        Log.w("AuthRepositoryImpl", "Aviso ao apagar subcoleção $subName", e)
                    }
                }
                userDocRef.delete().await()
            }

            // (b) Limpar o Room (clearAllTables), SharedPreferences/DataStore, cache e telemetria de IA
            database?.clearAllTables()

            try {
                com.studyflow.app.domain.ai.AICacheManager.clear()
            } catch (e: Exception) {
                Log.w("AuthRepositoryImpl", "Aviso ao limpar cache de IA", e)
            }

            privacyManager?.clearAll()

            context?.let { ctx ->
                try {
                    val rootDir = ctx.filesDir.parentFile
                    rootDir?.listFiles()?.forEach { file ->
                        if (file.isDirectory && file.name == "shared_prefs") {
                            file.listFiles()?.forEach { prefFile ->
                                ctx.getSharedPreferences(prefFile.nameWithoutExtension, Context.MODE_PRIVATE)
                                    .edit()
                                    .clear()
                                    .commit()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("AuthRepositoryImpl", "Aviso ao limpar SharedPreferences", e)
                }
            }

            // (c) Chamar auth.currentUser.delete()
            user.delete().await()

            // (d) signOut
            auth.signOut()
            Unit
        }
    }
}
