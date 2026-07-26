package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.SyncRepository
import com.example.domain.repository.SyncStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SyncViewModel(
    private val repository: SyncRepository
) : ViewModel() {

    val syncStatus: StateFlow<SyncStatus> = repository.syncStatus
        .stateIn(viewModelScope, SharingStarted.Lazily, SyncStatus.IDLE)
        
    val lastSyncTime: StateFlow<Long> = repository.lastSyncTime
        .stateIn(viewModelScope, SharingStarted.Lazily, 0L)

    fun syncNow() {
        viewModelScope.launch {
            repository.syncNow()
        }
    }
    
    fun backupNow() {
        viewModelScope.launch {
            repository.backupNow()
        }
    }
    
    fun restoreBackup() {
        viewModelScope.launch {
            repository.restoreBackup()
        }
    }
}
