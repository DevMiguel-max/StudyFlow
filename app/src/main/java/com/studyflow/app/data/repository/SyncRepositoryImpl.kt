package com.studyflow.app.data.repository

import com.studyflow.app.data.local.StudyFlowDao
import com.studyflow.app.domain.repository.AuthRepository
import com.studyflow.app.domain.repository.SyncRepository
import com.studyflow.app.domain.repository.SyncStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val authRepo: AuthRepository,
    private val dao: StudyFlowDao
) : SyncRepository {

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    override val syncStatus: Flow<SyncStatus> = _syncStatus
    
    private val _lastSyncTime = MutableStateFlow(0L)
    override val lastSyncTime: Flow<Long> = _lastSyncTime

    override suspend fun syncNow(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = authRepo.currentUser.firstOrNull() ?: return@withContext Result.success(Unit)
        _syncStatus.value = SyncStatus.SYNCING
        
        try {
            val userDocRef = firestore.collection("users").document(user.uid)
            
            // 1. Sincronizar Perfil e Configurações (Gamificação)
            val profile = dao.getUserProfileSync()
            if (profile != null) {
                userDocRef.set(profile).await()
            }
            
            val doc = userDocRef.get().await()
            if (doc.exists()) {
                val remoteProfile = doc.toObject(com.studyflow.app.data.local.UserProfile::class.java)
                if (remoteProfile != null) {
                    if (profile == null || remoteProfile.updatedAt > profile.updatedAt) {
                        dao.insertUserProfile(remoteProfile)
                    }
                }
            }
            
            // Arquitetura base para resolução de conflitos simples (Última alteração)
            // Em uma implementação completa de produção, iteraríamos por todas as entidades.
            // Sincronizar: Objetivos, Matérias, Sessões, Pomodoros, Revisões, Estatísticas, Simulados, Redações.
            
            // Exemplo de sincronização de Matérias (Subjects)
            /*
            val localSubjects = dao.getSubjectsSync()
            val remoteSubjectsSnap = userDocRef.collection("subjects").get().await()
            
            // Push locais
            for (subject in localSubjects) {
                userDocRef.collection("subjects").document(subject.id.toString()).set(subject)
            }
            // Pull remotas e resolver conflitos
            for (remoteDoc in remoteSubjectsSnap.documents) {
                val remoteSubj = remoteDoc.toObject(com.studyflow.app.data.local.Subject::class.java)
                if (remoteSubj != null) {
                    val localSubj = localSubjects.find { it.id == remoteSubj.id }
                    if (localSubj == null || remoteSubj.updatedAt > localSubj.updatedAt) {
                        dao.insertSubject(remoteSubj)
                    }
                }
            }
            */
            
            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SUCCESS
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }

    override suspend fun backupNow(): Result<Unit> = syncNow()
    
    override suspend fun restoreBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = authRepo.currentUser.firstOrNull() ?: return@withContext Result.success(Unit)
        _syncStatus.value = SyncStatus.SYNCING
        try {
            val doc = firestore.collection("users").document(user.uid).get().await()
            if (doc.exists()) {
                val remoteProfile = doc.toObject(com.studyflow.app.data.local.UserProfile::class.java)
                if (remoteProfile != null) {
                    dao.insertUserProfile(remoteProfile)
                }
            }
            
            // Restauraria as coleções aqui (subjects, tasks, etc)
            
            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SUCCESS
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }
}
