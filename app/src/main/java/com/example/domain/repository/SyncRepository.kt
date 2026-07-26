package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

enum class SyncStatus {
    IDLE, SYNCING, SUCCESS, ERROR
}

interface SyncRepository {
    val syncStatus: Flow<SyncStatus>
    val lastSyncTime: Flow<Long>
    
    suspend fun syncNow(): Result<Unit>
    suspend fun backupNow(): Result<Unit>
    suspend fun restoreBackup(): Result<Unit>
}
